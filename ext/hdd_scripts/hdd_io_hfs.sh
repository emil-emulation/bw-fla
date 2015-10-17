#!/bin/bash
set -e
set +x


# PARAMS CHECKING/HANDLING
USAGE='USAGE: hdd_hfs_io.sh op image.hfs io_data'

if [ "$#" -ne 3 ]; then echo -e "$USAGE"; exit 30; fi
OP="$1"; IMAGE="$2"; IO="$3"

if [ "$OP" != 'e' ] && [ "$OP" != 'i' ]
then
    echo "$USAGE"
    exit 40
fi

if [ ! -f "$IMAGE" ]
then
    echo "HFS image file not found under this path: '$IMAGE'"
    exit 50
fi

if [ ! -f "$IO" ] && [ ! -d "$IO" ]
then
    echo "Input/Output file/directory not found under this path: $IO"
    exit 60
fi


# RECURSIVE COPY TO "HFS" DRIVE
rCopyToHfs()
{
    local TARGET="$1"
    local DEST="$(basename "$TARGET")"
    DEST=$(echo "$DEST" | perl -pe 's/[^[:ascii:][:print:]]/_/g' | tr ': \t' '_' | sed 's/^[- \t]*//' | cut -c 1-31)

    if [ -d "$TARGET" ]
    then
        hmkdir "$DEST"
        hcd "$DEST"

        find "$TARGET" -maxdepth 1 -mindepth 1 | while read FILE
        do
            rCopyToHfs "$FILE"
        done

        hcd '::'
    else
        hcopy "$TARGET" :"$DEST"
    fi
}


# RECURSIVE COPY FROM "HFS" DRIVE
rCopyFromHfs()
{
    local DEST="$1"
    pushd "$DEST"

    hls -1abFQ | while read FILE
    do
        echo "$FILE"
        local FILE_TYPE="${FILE:(-1)}"
        local FILE_NAME="$(echo "$FILE" | sed -rn 's/"([^"]*)"[:,*]?/\1/p')"
       
         
        set +e; 
        # XXX: 'hls' return value cannot be used, since its always 0 
        hls "$FILE_NAME" |& grep $"no such file or directory" &>/dev/null 
        if [ $? -eq 0 ]
        then
            echo "file seems either not to exist or contains invalid characters in its name: $FILE_NAME" 1>&2
            continue
        fi
        set -e

        case $FILE_TYPE in
            ':')  
                hcd "$FILE_NAME"
                mkdir "$FILE_NAME"
                rCopyFromHfs "$FILE_NAME"
                hcd '::'
                ;;

            *) 
                hcopy "$FILE_NAME" "./"
                ;;&

            '*')
                chmod +x "$DEST/$FILE_NAME"
                ;;
        esac
    done
    
    popd
}


# CLEANUP HANDLING
trap cleanup EXIT ERR INT TERM QUIT
cleanup()
{
    local CODE=$?
    set +e
    humount "$IMAGE" &> /dev/null
    exit $CODE 
}


# MOUNTING, PERFORMING DESIRED OPERATION
(
    flock -w 20 200
    hmount "$IMAGE" && hcd
    
    if [ $OP == 'i' ]
    then
        rCopyToHfs "$IO"
    else
        rCopyFromHfs "$IO"
    fi

)   200>/var/lock/hdd_io_hfs.sh.wl
