
_Help version: ${program_version}_

${docker_inspector_name} is invoked by ${detect_product_name} when ${detect_product_name} is given a Docker image to scan.
${docker_inspector_name} will:.

1. Discover packages (components) installed in a given Linux image by analyzing the contents of the Linux package manager database.
2. Provide to ${detect_product_name}, for any image, potentially useful targets (file archives) for signature and binary scanning.

${docker_inspector_name} does not run the target image, so it is safe to run it on untrusted images.

While earlier versions of ${docker_inspector_name} could be run standalone,
the only way to use ${docker_inspector_name} now and in the future is
to ${detect_product_name} on a Docker image. When you run ${detect_product_name}
on a Docker image, it will automatically run 
${docker_inspector_name}. See the ${detect_product_name} documentation for more information on
running ${detect_product_name}.

## Package (component) discovery

For package discovery on a Linux image, ${docker_inspector_name} extracts the Linux package manager
database from the image, and utilizes the appropriate Linux package manager to provide a list of
the installed packages, which
it returns to ${detect_product_name} in BDIO (Black Duck Input Output) format.
Because it relies on the Linux package manager as its source of this data,
the discovered packages are limited to those installed and managed using the Linux package manager.

${docker_inspector_name} can discover package manager-installed components in
Linux Docker images that use the DPKG, RPM, or APK package manager database formats.

## Signature and binary scan targets

Signature and binary scan targets contain the container file system.
The container file system
is the file system that a container created from the target image would start with. The
container file system is (by default) returned to ${detect_product_name} in two forms:
as an archive file that contains the container file system (the preferred format for binary
scanning), and as a saved squashed (single layer) image
that contains the container file system (the preferred format for signature scanning).

## Non-linux images

When run on a non-Linux image (for example, a Windows image,
or an image that contain no operating system), ${docker_inspector_name}
will return to ${detect_product_name} a BDIO file with zero components
along with the signature and binary scan targets.
Components may be discovered for these images
during the signature and/or binary scanning perfomed by
${detect_product_name}.

## Modes of operation

${docker_inspector_name} has two modes:

* Host mode, for running on a server or virtual machine (VM) where ${docker_inspector_name} can perform Docker operations using a Docker Engine.
* Container mode, for running inside a container started by Docker, Kubernetes, OpenShift, and others.

In either mode, ${docker_inspector_name} runs as a ${detect_product_name} inspector to extend the capaibilities of ${detect_product_name}.
${docker_inspector_name} is more complex than most ${detect_product_name} inspectors because it relies on container-based services
(the image inspector services)
to perform its job. When running on a host machine that has access to a Docker Engine ("host mode"),
${docker_inspector_name} can start and manage the image inspector services (containers) automatically.
When ${detect_product_name} and ${docker_inspector_name} are running within a Docker container
("container mode"), the image inspector services must be started and managed by the user or
the container orchestration system.

### Host mode

Host mode (the default) is for servers/VMs where ${docker_inspector_name} can perform Docker operations (such as pulling an image)
using a Docker Engine.

Host mode requires that ${docker_inspector_name} can access a Docker Engine. https://github.com/docker-java/docker-java utilizes the
[docker-java library](https://github.com/docker-java/docker-java) to act as a client of that Docker Engine.
This enables ${docker_inspector_name} to pull the target image from a Docker registry such
as Docker Hub. Alternatively, you can save an image to a .tar file by using the *docker save* command. Then, run ${docker_inspector_name}
on the .tar file. ${docker_inspector_name} supports Docker Image Specification v1.2.0 format .tar files.

In Host mode, ${docker_inspector_name} can also pull, run, stop, and remove the image inspector service images as needed,
greatly simplifying usage, and greatly increasing run time.

### Container mode

Container mode is for container orchestration environments (Kubernetes, OpenShift, etc.)
where ${detect_product_name} and ${docker_inspector_name} run
inside a container where ${docker_inspector_name} cannot perform Docker operations.
For information on running ${docker_inspector_name} in container mode,
refer to [Deploying](deployment.md).

It possible to utilize container mode when running ${detect_product_name} and ${docker_inspector_name} on a host
that supports host mode. Container mode is more difficult to manage than host mode,
but you might choose container mode in order to increase throughput (to scan more images per hour).
Most of the time spent by ${docker_inspector_name} running in host mode is spent starting and stopping the image inspector services.
When these services are already running (in the usual sense of the word "service")
as they do in container mode,
${docker_inspector_name} much more quickly than it would in host mode.

## Requirements

Requirements for including ${docker_inspector_name} in a ${detect_product_name} run
include of all of ${detect_product_name}'s requirements plus:

* Three available ports for the image inspector services. By default, these ports are 9000, 9001, and 9002.
* The environment must be configured so that files created by ${docker_inspector_name} are readable by all. On Linux, this means an appropriate umask value (for example, 002 or 022 would work). On Windows, this means the
Detect "output" directory (controlled by the ${detect_product_name} property *detect.output.path*)
must be readable by all.
* In host mode: access to a Docker Engine versions 17.09 or higher.
* In container mode: you must start the ${docker_inspector_name} container that meets the preceding requirements, and three container-based
"image inspector" services. All four of these containers must share a mounted volume and be able to reach each other through HTTP GET operations using base URLs
that you provide. For more information, refer to [Deploying](deployment.md).
    
## Running ${docker_inspector_name}

To invoke ${docker_inspector_name}, pass a docker image to 
${detect_product_name} via one of the following properties:

* detect.docker.image
* detect.docker.image.id
* detect.docker.image
* detect.docker.tar

See the ${detect_product_name} documentation for details.

## Advanced usage

The most common cases of ${docker_inspector_name} can be configured using 
${detect_product_name} properties. However, there are scenarios (including container mode)
that require access to ${docker_inspector_name} properties for which there is no corresponding
${detect_product_name} property. To set these 
${docker_inspector_name} properties, use the
${detect_product_name} detect.docker.passthrough property
(see the ${detect_product_name} documentation for details on how to use detect.docker.passthrough).

## Transitioning from Black Duck Docker Inspector to ${detect_product_name}

If you have been running the Black Duck Docker Inspector directly, and need to transition to
invoking ${docker_inspector_name} from ${detect_product_name}, here are some recommendations likely to 
help you make the transition:

1. If you run Black Duck Docker Inspector with `blackduck-docker-inspector.sh`, replace `blackduck-docker-inspector.sh` in your command line with `detect7.sh` (adjust the ${detect_product_name} major version as necessary).
See the ${detect_product_name} documentation for information on where to get the ${detect_product_name} script.
1. If you run Black Duck Docker Inspector with `java -jar blackduck-docker-inspector-{version}.jar`, replace `blackduck-docker-inspector-{version}.jar` in your command line with `synopsys-detect-{version}.jar`.
See the ${detect_product_name} documentation for information on where to get the ${detect_product_name} .jar.
1. For each of the following properties used in your command line, add `detect.` to the beginning of the property name: docker.image, docker.image.id, docker.tar, docker.platform.top.layer.id. For example, change `--docker.image=repo:tag` with `--detect.docker.image=repo:tag`.
1. For all other Docker Inspector properties used in your command line, add `detect.docker.passthrough.` to the beginning of the property name. For example, change `--bdio.organize.components.by.layer=true` to `--detect.docker.passthrough.bdio.organize.components.by.layer=true`.
