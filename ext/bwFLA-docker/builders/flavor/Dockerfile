# DERIVING FROM MINIMALISTIC BWFLA IMAGE:
FROM eaas/bwfla:base

# EXECUTED DURING IMAGE BUILD:
ADD bw-fla/ext/bwFLA-docker/builders/flavor/flavor-install     /home/bwfla/flavor-install
ADD bw-fla/ext/bwFLA-docker/builders/flavor/flavor-start       /home/bwfla/flavor-start
ADD bw-fla/ext/appserver                                       /home/bwfla/appserver
ADD bw-fla/ext/fits                                            /home/bwfla/misc/fits
ADD bw-fla/ext/hdl-keys/admpriv.bin                            /home/bwfla/misc/admpriv.bin 
ADD bw-fla/ext/hdd_scripts                                     /home/bwfla/misc/hdd_scripts
ADD bw-fla/ext/floppy_scripts                                  /home/bwfla/misc/floppy_scripts

RUN rm -rf /home/bwfla/appserver/standalone/configuration/standalone_xml_history
RUN bash /home/bwfla/flavor-install

# EXECUTED WHEN CONTAINER IS RUN:
CMD ["bash", "/home/bwfla/flavor-start"]
