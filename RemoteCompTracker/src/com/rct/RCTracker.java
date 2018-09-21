/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rct;

import java.io.*;
import java.net.InetAddress;
import java.time.*;
import java.util.*;

/**
 *
 * @author JWizard
 */
public class RCTracker {

    public static void main(String[] args) throws IOException, InterruptedException {

        /**
         * Here I need an init() method for writing to the output file every
         * time the application starts. This will help in tracking whether the
         * system was running or not.
		*
         */
        //This list will hold all the devices.
        ArrayList<Device> devices = new ArrayList<>();
        ArrayList<Device> onlineList = new ArrayList<>();
        ArrayList<Device> offlineList = new ArrayList<>();

        //This is the output file
        PrintWriter outputFile = new PrintWriter(new BufferedWriter(new FileWriter("c:\\rct\\output.txt", true)));

        //Get devices from the file
        devices = initDevices();

        //Log to the file that the system is starting
        init(outputFile);
        logEntryFirstTime(devices, onlineList, offlineList, outputFile);

        for (Device d : onlineList) {
            System.out.println("onlineList");
            System.out.println(d.getIpAddress() + "|" + d.getDescription());

        }
        for (Device d : offlineList) {
            System.out.println("offlineList");
            System.out.println(d.getIpAddress() + "|" + d.getDescription());

        }

        //Here, loop
        while (true) {
            Thread.sleep(1000);
            System.out.println("after time lapse");

            //Offline list.
            for (int i = offlineList.size() - 1; i >= 0; i--) {
                Device temp = offlineList.get(i);
                try {
                    System.out.println("Status: " + temp.getIpAddress() + ":" + ping(temp.getIpAddress()));
                    if (ping(temp.getIpAddress())) {
                        //log to the file
                        outputFile.println(
                                temp.getIpAddress() + "|"
                                + temp.getDescription() + "|"
                                + "true" + "|"
                                + LocalDateTime.now().toString());
                        outputFile.flush();
                        onlineList.add(temp);
                        offlineList.remove(temp);
                    }//END IF
                } catch (Exception e) {
                    System.out.println("Exception raised.");
                }
            }

            //Online list
            for (int i = onlineList.size() - 1; i >= 0; i--) {
                Device temp = onlineList.get(i);
                try {
                    System.out.println("Status: " + temp.getIpAddress() + ":" + ping(temp.getIpAddress()));
                    if (!ping(temp.getIpAddress())) {
                        //log to the file
                        outputFile.println(
                                temp.getIpAddress() + "|"
                                + temp.getDescription() + "|"
                                + "false" + "|"
                                + LocalDateTime.now().toString());
                        outputFile.flush();
                        offlineList.add(temp);
                        onlineList.remove(temp);
                    }//END IF
                } catch (Exception e) {
                    System.out.println("Exception raised.");
                }
            }
        }

    }//End Main method here.

    /**
     * Log in devices first time the system starts - Everytime the system
     * starts, it must log entries for each device in the list.
*
     */
    private static void logEntryFirstTime(
            ArrayList<Device> devices,
            ArrayList<Device> onlineList,
            ArrayList<Device> offlineList, PrintWriter outputFile) throws IOException, InterruptedException {

        String toPrintLine = "";

        for (int i = 0; i < devices.size(); i++) {
            Device temp = new Device();
            temp = devices.get(i);
            Boolean status = new Boolean(ping(temp.getIpAddress()));
            toPrintLine
                    = temp.getIpAddress() + "|" + temp.getDescription() + "|" + status.toString() + "|" + LocalDateTime.now();
            outputFile.println(toPrintLine);
            if (status) {
                //add to onlineList
                onlineList.add(temp);
            } else {
                //add to offlineList
                offlineList.add(temp);
            }
            outputFile.flush();
        }

    }

    /**
     * This method is one that checks whether an IP of a device is available or
     * not. It returns boolean,true or false. this becomes the status of the
     * device.
*
     */
    private static boolean ping(String ipAddress) {
        boolean reachable = false;
        try {

            InetAddress address = InetAddress.getByName(ipAddress);
            if (address.isReachable(10000)) {
                reachable = true;
            } else {
                reachable = false;
            }
        } catch (Exception i) {
            System.out.println("IE");
        }
        return reachable;
    }

//This method is executed once everytime the system starts.
    private static void init(PrintWriter pw) throws IOException {
        String strokes = "-----------------------------------------------";
        String middle = "///System starting entry:" + LocalDateTime.now() + "///";
        pw.println(strokes);
        pw.println(middle);
        pw.println(strokes);

    }

    /**
     * This method is responsible for parsing the file containing the devices It
     * will return an array of these devises
*
     */
    public static ArrayList<Device> initDevices() throws IOException {
        ArrayList<Device> devices = new ArrayList<>();
        ArrayList<String> rawStrings = new ArrayList<>();
        ArrayList<String> ips = new ArrayList<>();
        String[] loc_desc = new String[2];

        //Read from the file & store in an AL.
        BufferedReader reader = new BufferedReader(new FileReader("c:\\rct\\file.txt"));
        String line = reader.readLine();
        while (line != null) {
            rawStrings.add(line);
            line = reader.readLine();
        }

        //String IPs from the read lines
        for (String s : rawStrings) {
            StringBuffer temp = new StringBuffer();
            for (int i = 0; i < s.length(); i++) {
                if (s.charAt(i) != '|') {
                    temp = temp.append(s.charAt(i));
                }
                if (s.charAt(i) == '|') {
                    break;
                }
            }
            //Store valid IPs only
            if (isValidIP(temp.toString())) {
                ips.add(temp.toString());
            }

        }

        //Now pull the Location & Description of the device
        for (int i = 0; i < ips.size(); i++) {
            String tempIP = ips.get(i);
            int tempLength = tempIP.length();
            String line_ = rawStrings.get(i);
            loc_desc = getLocDesc(line_, tempLength);

            //Now create a device, those that have a loc & desc only
            if (loc_desc[0] != null && loc_desc[1] != null) {
                Device tempDevice = new Device();
                tempDevice.setIpAddress(ips.get(i));
                tempDevice.setLocation(loc_desc[0]);
                tempDevice.setDescription(loc_desc[1]);
                devices.add(tempDevice);
            }
        }

        return devices;
    }

    /**
     * The method below returns the location & description of the device if it
     * is available in the read line.
*
     */
    public static String[] getLocDesc(String line, int endOfIP) {
        int endIndex = 0;
        String location = null;
        String descritpion = null;
        String[] loc_desc = new String[2];
        for (int i = endOfIP + 1; i < line.length(); i++) {
            if (line.charAt(i) == '|') {
                loc_desc[0] = line.substring(endOfIP + 1, i);
                loc_desc[1] = line.substring(i + 1, line.length());
                break;
            }
        }
        return loc_desc;
    }

//Validate all IPs before acceptiong them.
    public static boolean isValidIP(String ip) {
        /*
		I took the regex below from a Patterns class on net, tested it 
		with serveral IPs but have not xhausted all possibilities.
		**/
        return ip.matches(
                "((25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}|[1-9][0-9]|[1-9])\\.(25[0-5]|2[0-4]"
                + "[0-9]|[0-1][0-9]{2}|[1-9][0-9]|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|[0-1]"
                + "[0-9]{2}|[1-9][0-9]|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}"
                + "|[1-9][0-9]|[0-9]))");
    }

}
