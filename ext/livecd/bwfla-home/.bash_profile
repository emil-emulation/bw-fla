
if [ -z $DISPLAY ] && [ "$XDG_VTNR" == "1" ]
then
	exit
fi

clear

echo "Welcome to the EMIL-LiveCD!"
echo ""

# Autostart browser
./start-workflow.sh
clear

while true
do
	echo "Please, choose next action: "
	echo "1) Start Local-Workflow"
	echo "2) Shutdown Live-System"
	echo "3) Terminate this dialog"
	echo -n "=> "
	read choice
	case "$choice" in
		1) ./start-workflow.sh; clear ;;
		2) sudo poweroff ;;
		3) break ;;
		*) echo "Invalid choice!" ;;
	esac
	echo ""
done

