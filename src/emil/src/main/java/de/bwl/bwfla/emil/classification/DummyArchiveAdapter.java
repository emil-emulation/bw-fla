package de.bwl.bwfla.emil.classification;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;

import de.bwl.bwfla.common.datatypes.Environment;
import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.emil.ArchiveAdapter;
import de.bwl.bwfla.emil.ArchiveAdapter.EnvironmentInfo;

public class DummyArchiveAdapter implements ArchiveAdapter {
    public final static Map<URL, List<Environment>> OBJ_ENVS = new HashMap<>();
   
    static
    {   
        try
        {
            Environment mac76 = Environment.fromValue("<emulationEnvironment xmlns=\"http://bwfla.bwl.de/common/datatypes\"> <id>3002</id> <arch>ppc</arch> <description><title>Apple Mac OS 7.6 (imal)</title></description> <emulator bean=\"SheepShaver\" />  <drive>  <data>binding://main_hdd</data>  <iface>ide</iface>  <bus>0</bus>  <unit>0</unit>  <type>disk</type>  <boot>true</boot>  <plugged>true</plugged> </drive><drive>  <data></data>  <iface>ide</iface>  <bus>0</bus>  <unit>1</unit>  <type>cdrom</type> </drive> <binding id=\"main_hdd\">  <url>nbd:132.230.4.10:10809:exportname=os7.6.dsk</url>  <access>cow</access> </binding> <binding id=\"apple_roms\">  <url>nbd:132.230.4.10:10809:exportname=apple_roms.iso</url>  <access>cow</access> </binding>  <nativeConfig linebreak=\"[[nl]]\">extfs /dev/null[[nl]]screen dga/800/600[[nl]]windowmodes 7[[nl]]screenmodes 0[[nl]]seriala /dev/ttyS0[[nl]]serialb /dev/ttyS1[[nl]]rom binding://apple_roms/96CD923D.rom[[nl]]bootdrive 0[[nl]]bootdriver 0 [[nl]]ramsize 268435456[[nl]]frameskip 0[[nl]]gfxaccel false[[nl]]nonet false[[nl]]ether slirp [[nl]]nosound false[[nl]]nogui true[[nl]]nocdrom false[[nl]]noclipconversion false[[nl]]ignoresegv true[[nl]]ignoreillegal true[[nl]]jit true[[nl]]jit68k true[[nl]]keyboardtype 5[[nl]]keycodes false[[nl]]mousewheelmode 1[[nl]]mousewheellines 3[[nl]]ignoresegv true[[nl]]idlewait true [[nl]] </nativeConfig></emulationEnvironment>");
            Environment mac9  = Environment.fromValue("<emulationEnvironment xmlns=\"http://bwfla.bwl.de/common/datatypes\"> <id>3004</id> <arch>ppc</arch> <description><title>Apple Mac OS 9</title></description> <emulator bean=\"SheepShaver\" />  <drive>  <data>binding://main_hdd</data>  <iface>ide</iface>  <bus>0</bus>  <unit>0</unit>  <type>disk</type>  <boot>true</boot> </drive> <drive>  <data></data>  <iface>ide</iface>  <bus>1</bus>  <unit>0</unit>  <type>disk</type>  <boot>false</boot>  <filesystem>HFS</filesystem> </drive> <drive>  <data></data>  <iface>ide</iface>  <bus>0</bus>  <unit>1</unit>  <type>cdrom</type> </drive><binding id=\"apple_roms\">  <url>nbd:132.230.4.10:10809:exportname=apple_roms.iso</url>  <access>cow</access> </binding> <binding id=\"main_hdd\">  <url>nbd:132.230.4.10:10809:exportname=sheepshaver-MacOS_9.raw</url>  <access>cow</access> </binding> <nativeConfig linebreak=\"[[nl]]\">extfs /dev/null[[nl]]screen dga/1024/768[[nl]]windowmodes 7[[nl]]screenmodes 0[[nl]]seriala /dev/ttyS0[[nl]]serialb /dev/ttyS1[[nl]]rom binding://apple_roms/newworld86.rom[[nl]]bootdrive 0[[nl]]bootdriver 0 [[nl]] ramsize 268435456[[nl]]frameskip 0[[nl]]gfxaccel false[[nl]]nonet false[[nl]]ether slirp [[nl]]nosound false[[nl]]nogui true[[nl]]nocdrom false[[nl]]noclipconversion false[[nl]]ignoresegv true[[nl]]ignoreillegal true[[nl]]jit true[[nl]]jit68k true[[nl]]keyboardtype 5[[nl]]keycodes false[[nl]]mousewheelmode 1[[nl]]mousewheellines 3[[nl]]ignoresegv true[[nl]]idlewait true [[nl]]</nativeConfig></emulationEnvironment>");
            Environment win98 = Environment.fromValue("<emulationEnvironment xmlns=\"http://bwfla.bwl.de/common/datatypes\"> <id>4404</id> <description><title>Microsoft Windows 98 (SE)</title></description> <arch>i386</arch> <emulator bean=\"VirtualBox\">   <machine>     --memory 1024     --mouse usbtablet     --hwvirtex on     --ioapic on     --audio sdl     --audiocontroller sb16     --nic1 nat     --nictype1 Am79C973   </machine> </emulator><drive>  <data></data>  <iface>ide</iface>  <bus>0</bus>  <unit>1</unit>  <type>cdrom</type>  <filesystem>ISO</filesystem> </drive><drive>  <data></data>  <iface>floppy</iface>  <bus>0</bus>  <unit>0</unit>  <type>floppy</type>  <boot>false</boot>  <filesystem>FAT12</filesystem> </drive> <drive>  <data>binding://main_hdd</data>  <iface>ide</iface>  <bus>0</bus>  <unit>0</unit>  <type>disk</type>  <boot>true</boot>  <plugged>true</plugged> </drive> <binding id=\"main_hdd\">  <url>nbd:132.230.4.10:10809:exportname=virtualbox-MS_Windows_98.vdi</url>  <access>cow</access> </binding><nativeConfig>setextradata $VMUUID VBoxInternal/USB/HidMouse/0/Config/CoordShift 0</nativeConfig></emulationEnvironment>");
            Environment winXp = Environment.fromValue("<emulationEnvironment xmlns=\"http://bwfla.bwl.de/common/datatypes\"> <id>4019</id> <description><title>Microsoft Windows XP</title></description> <arch>i386</arch> <emulator bean=\"VirtualBox\" /><drive>  <data></data>  <iface>ide</iface>  <bus>0</bus>  <unit>1</unit>  <type>cdrom</type>  <filesystem>ISO</filesystem> </drive><drive>  <data></data>  <iface>floppy</iface>  <bus>0</bus>  <unit>0</unit>  <type>floppy</type>  <boot>false</boot>  <filesystem>FAT12</filesystem> </drive> <drive>  <data>binding://main_hdd</data>  <iface>ide</iface>  <bus>0</bus>  <unit>0</unit>  <type>disk</type>  <boot>true</boot>  <plugged>true</plugged> </drive> <binding id=\"main_hdd\">  <url>nbd:132.230.4.10:10809:exportname=virtualbox-MS_Windows_XP_Pro_neu.vdi</url>  <access>cow</access> </binding></emulationEnvironment>");
            
            URL chopSueyRef = new URL("http://132.230.4.15/objects/ISO/CHOPSUEY/CHOPSUEY.iso");
            OBJ_ENVS.put(chopSueyRef, Arrays.asList(mac76, mac9));

            URL klugeRef = new URL("http://132.230.4.15/objects/ub/KLUGE/KLUGE.iso");
            OBJ_ENVS.put(klugeRef, Arrays.asList(win98, winXp));
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
    
    @Override
    public String getFileCollectionForObject(String objectId) throws NoSuchElementException, BWFLAException {
        for(Entry<URL, List<Environment>> entry: OBJ_ENVS.entrySet())
        {
            String[] urlParts = entry.getKey().getPath().split("/");
            String id = urlParts[urlParts.length-1];
            
            if(id.equalsIgnoreCase(objectId))
            {
                return entry.getKey().toString();
            }
        }
        throw new NoSuchElementException("An object with id " + objectId + " is not part of this archive.");
    }

    @Override
    public String getMetadataForObject(String objectId) throws NoSuchElementException, BWFLAException {
        String test = "{ \"status\": \"0\", \"title\": \"Deutscher Bundestag multimedial\", \"metadata\": [  {\"label\": \"XYZ Serial\", \"value\": \"AS123F-DFF2-22SF-FF11A\"},  {\"label\": \"Beschreibung\", \"value\": \"Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna.\"} ]}";
        return test;
    }

    @Override
    public List<EnvironmentInfo> getEnvironmentsForObject(String objectId) throws NoSuchElementException, BWFLAException {
        for(Entry<URL, List<Environment>> entry: OBJ_ENVS.entrySet())
        {
            String[] urlParts = entry.getKey().getPath().split("/");
            String id = urlParts[urlParts.length-1];
            
            if(id.equalsIgnoreCase(objectId))
            {
                List<EnvironmentInfo> result = new ArrayList<EnvironmentInfo>();
                for (Environment e : entry.getValue()) {
                    result.add(new EnvironmentInfo(e.getId(), e.getDescription().getTitle()));
                }
                return result;
            }
        }
        throw new NoSuchElementException("An object with id " + objectId + " is not part of this archive.");
    }

}
