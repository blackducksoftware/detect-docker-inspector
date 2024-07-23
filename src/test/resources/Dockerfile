FROM centos@sha256:773bb32cbd1d171da7df52de8d7b731320748707b821bdbabb885d6eda894e87
ENV LANG=en_US.UTF-8

RUN rm -f /etc/yum.repos.d/CentOS-Base.repo
COPY CentOS-Base.repo /etc/yum.repos.d/CentOS-Base.repo
RUN rpm -e vim-minimal && yum install -y bacula-director-5.2.13-23.1.el7 bacula-storage-5.2.13-23.1.el7 bacula-client-5.2.13-23.1.el7 bacula-console-5.2.13-23.1.el7
