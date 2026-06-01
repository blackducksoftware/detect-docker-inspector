/**
 * blackduck-docker-inspector
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.blackduck.dockerinspector.dockerclient;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

/**
 * Handles extracting a specific image from an OCI-format multi-image tar (produced by
 * "docker save image1 image2 ...") into a new single-image tar.
 *
 * Docker 25+ changed "docker save" to produce OCI Image Layout format where all layers
 * are stored as flat blobs under blobs/sha256/ rather than classic &lt;hash&gt;/layer.tar
 * directories.  The image inspector service (v6.2.1) does not correctly select a specific
 * image by repo:tag from a multi-image OCI tar — it always returns the first image.
 * This extractor works around that by pre-filtering the tar so the service always
 * receives a single-image tar containing:
 * <ul>
 *   <li>a new {@code index.json} referencing only the requested image's OCI manifest blob</li>
 *   <li>the OCI manifest blob itself ({@code blobs/sha256/&lt;manifest-digest&gt;})</li>
 *   <li>the image config blob and all layer blobs for that image</li>
 *   <li>a new {@code manifest.json} (Docker format) with a single entry</li>
 *   <li>a new {@code repositories} file with a single image entry</li>
 *   <li>{@code oci-layout}</li>
 * </ul>
 */
public class OciMultiImageTarExtractor {

    private static final Logger logger = LoggerFactory.getLogger(OciMultiImageTarExtractor.class);

    /** POJO matching each entry in Docker-format manifest.json */
    private static class DockerManifestEntry {
        String Config;
        List<String> RepoTags;
        List<String> Layers;
    }

    /**
     * If the given tar is an OCI multi-image tar AND repo+tag are specified,
     * extract just the requested image into a new temp single-image tar and return it.
     *
     * Returns {@code null} when extraction is not needed or not possible.
     */
    public File extractSingleImageTar(File multiImageTar, String imageRepo, String imageTag) throws IOException {
        if (imageRepo == null || imageRepo.isEmpty() || imageTag == null || imageTag.isEmpty()) {
            return null;
        }

        // ── Step 1: read Docker manifest.json ────────────────────────────────
        String dockerManifestContent = readEntryFromTar(multiImageTar, "manifest.json");
        if (dockerManifestContent == null) {
            logger.debug("No manifest.json found in tar — skipping OCI multi-image extraction");
            return null;
        }

        Gson gson = new Gson();
        Type listType = new TypeToken<List<DockerManifestEntry>>() {}.getType();
        List<DockerManifestEntry> dockerEntries = gson.fromJson(dockerManifestContent, listType);

        if (dockerEntries == null || dockerEntries.size() <= 1) {
            logger.debug("Tar has {} image(s) in manifest.json — no extraction needed",
                dockerEntries == null ? 0 : dockerEntries.size());
            return null;
        }

        logger.info("Tar contains {} images; extracting {}:{}", dockerEntries.size(), imageRepo, imageTag);

        // ── Step 2: find the matching Docker manifest entry ───────────────────
        String requestedRepoTag = imageRepo + ":" + imageTag;
        DockerManifestEntry matchedDockerEntry = null;
        for (DockerManifestEntry entry : dockerEntries) {
            if (entry.RepoTags != null) {
                for (String repoTag : entry.RepoTags) {
                    if (repoTag.equals(requestedRepoTag)) {
                        matchedDockerEntry = entry;
                        break;
                    }
                }
            }
            if (matchedDockerEntry != null) break;
        }

        if (matchedDockerEntry == null) {
            logger.warn("Image {}:{} not found in manifest.json — using tar as-is", imageRepo, imageTag);
            return null;
        }

        // ── Step 3: collect blob paths from the Docker manifest entry ─────────
        Set<String> blobsToInclude = new HashSet<>();
        if (matchedDockerEntry.Config != null) {
            blobsToInclude.add(normalizePath(matchedDockerEntry.Config));
        }
        if (matchedDockerEntry.Layers != null) {
            for (String layer : matchedDockerEntry.Layers) {
                blobsToInclude.add(normalizePath(layer));
            }
        }

        // ── Step 4: handle OCI index.json — find + include the OCI manifest blob
        String newIndexJson = null;
        String indexContent = readEntryFromTar(multiImageTar, "index.json");
        if (indexContent != null) {
            JsonObject indexObj = gson.fromJson(indexContent, JsonObject.class);
            JsonArray manifests = indexObj.getAsJsonArray("manifests");
            JsonObject matchedOciEntry = null;

            if (manifests != null) {
                for (JsonElement elem : manifests) {
                    JsonObject m = elem.getAsJsonObject();
                    JsonObject annotations = m.getAsJsonObject("annotations");
                    if (annotations != null) {
                        // The annotation uses the full docker.io/ prefix
                        String imageName = getAnnotationValue(annotations, "io.containerd.image.name");
                        if (imageName != null &&
                            (imageName.equals(requestedRepoTag) ||
                             imageName.equals("docker.io/" + requestedRepoTag))) {
                            matchedOciEntry = m;
                            break;
                        }
                    }
                }
            }

            if (matchedOciEntry != null) {
                // Include the OCI manifest blob itself
                String digest = matchedOciEntry.get("digest").getAsString();
                // digest is "sha256:<hash>" → blob path is "blobs/sha256/<hash>"
                String ociManifestBlobPath = "blobs/sha256/" + digest.replace("sha256:", "");
                blobsToInclude.add(ociManifestBlobPath);

                // Build a new index.json with only this one manifest entry
                JsonArray singleManifests = new JsonArray();
                singleManifests.add(matchedOciEntry);
                indexObj.add("manifests", singleManifests);
                newIndexJson = gson.toJson(indexObj);
            } else {
                logger.warn("Image {}:{} not found in index.json annotations", imageRepo, imageTag);
                // Still build an empty-manifests index so the service doesn't fail on reading it
                if (manifests != null) {
                    indexObj.add("manifests", new JsonArray());
                    newIndexJson = gson.toJson(indexObj);
                }
            }
        }

        // ── Step 5: build new Docker manifest.json (single entry) ─────────────
        List<DockerManifestEntry> singleDockerList = new ArrayList<>();
        DockerManifestEntry singleDockerEntry = new DockerManifestEntry();
        singleDockerEntry.Config = matchedDockerEntry.Config;
        singleDockerEntry.RepoTags = matchedDockerEntry.RepoTags;
        singleDockerEntry.Layers = matchedDockerEntry.Layers;
        singleDockerList.add(singleDockerEntry);
        String newDockerManifest = gson.toJson(singleDockerList);

        // ── Step 6: build new repositories file ──────────────────────────────
        Map<String, Map<String, String>> repos = new HashMap<>();
        Map<String, String> tagToHash = new HashMap<>();
        if (matchedDockerEntry.Layers != null && !matchedDockerEntry.Layers.isEmpty()) {
            String lastLayer = matchedDockerEntry.Layers.get(matchedDockerEntry.Layers.size() - 1);
            String hash = lastLayer.replaceFirst("^blobs/sha256/", "").replaceFirst("^\\./blobs/sha256/", "");
            tagToHash.put(imageTag, hash);
        }
        repos.put(imageRepo, tagToHash);
        String newRepositories = gson.toJson(repos);

        // ── Step 7: write the new single-image tar ────────────────────────────
        // Preserve the original tar filename so that downstream code which derives
        // output filenames (e.g. aggregated_containerfilesystem.tar.gz) from the
        // tar name continues to produce the names the tests and callers expect.
        File tempDir = new File(System.getProperty("java.io.tmpdir"),
            "docker_inspector_oci_extract_" + System.nanoTime());
        tempDir.mkdirs();
        tempDir.deleteOnExit();
        File newTar = new File(tempDir, multiImageTar.getName());
        newTar.deleteOnExit();

        try (TarArchiveOutputStream out = new TarArchiveOutputStream(
                new BufferedOutputStream(new FileOutputStream(newTar)))) {
            out.setLongFileMode(TarArchiveOutputStream.LONGFILE_POSIX);

            writeStringEntry(out, "manifest.json", newDockerManifest);
            writeStringEntry(out, "repositories", newRepositories);

            if (newIndexJson != null) {
                writeStringEntry(out, "index.json", newIndexJson);
            }

            // Copy oci-layout verbatim
            String ociLayout = readEntryFromTar(multiImageTar, "oci-layout");
            if (ociLayout != null) {
                writeStringEntry(out, "oci-layout", ociLayout);
            }

            // Copy only the needed blobs from the original tar
            copyBlobEntries(multiImageTar, blobsToInclude, out);
        }

        logger.info("Extracted image {}:{} from multi-image tar → {}", imageRepo, imageTag, newTar.getAbsolutePath());
        return newTar;
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    /** Read a named entry from a tar file; returns null if not found. */
    private String readEntryFromTar(File tarFile, String entryName) throws IOException {
        try (TarArchiveInputStream in = new TarArchiveInputStream(
                new BufferedInputStream(new FileInputStream(tarFile)))) {
            TarArchiveEntry entry;
            while ((entry = in.getNextTarEntry()) != null) {
                if (normalizePath(entry.getName()).equals(entryName)) {
                    return new String(IOUtils.toByteArray(in), StandardCharsets.UTF_8);
                }
            }
        }
        return null;
    }

    /** Copy only the required blob entries (and their parent dirs) into the output tar. */
    private void copyBlobEntries(File sourceTar, Set<String> blobPaths, TarArchiveOutputStream out) throws IOException {
        try (TarArchiveInputStream in = new TarArchiveInputStream(
                new BufferedInputStream(new FileInputStream(sourceTar)))) {
            TarArchiveEntry entry;
            while ((entry = in.getNextTarEntry()) != null) {
                String normalizedName = normalizePath(entry.getName());
                boolean isNeededBlob = blobPaths.contains(normalizedName);
                // Keep directory entries so the tar is well-formed
                boolean isBlobDir = normalizedName.equals("blobs/") || normalizedName.equals("blobs/sha256/");

                if (isNeededBlob || isBlobDir) {
                    TarArchiveEntry newEntry = new TarArchiveEntry(entry.getName());
                    newEntry.setSize(entry.getSize());
                    newEntry.setMode(entry.getMode());
                    out.putArchiveEntry(newEntry);
                    if (!entry.isDirectory()) {
                        IOUtils.copy(in, out);
                    }
                    out.closeArchiveEntry();
                }
            }
        }
    }

    /** Write a String as a new tar entry. */
    private void writeStringEntry(TarArchiveOutputStream out, String name, String content) throws IOException {
        byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
        TarArchiveEntry entry = new TarArchiveEntry(name);
        entry.setSize(bytes.length);
        out.putArchiveEntry(entry);
        out.write(bytes);
        out.closeArchiveEntry();
    }

    /** Strip leading "./" from tar entry names for consistent comparison. */
    private String normalizePath(String path) {
        if (path == null) return "";
        return path.startsWith("./") ? path.substring(2) : path;
    }

    /** Safely get an annotation string value from a JsonObject. */
    private String getAnnotationValue(JsonObject annotations, String key) {
        JsonElement el = annotations.get(key);
        return el != null && !el.isJsonNull() ? el.getAsString() : null;
    }
}

