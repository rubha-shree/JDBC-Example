package com.company;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.regex.*;

class InvalidInputException extends Exception {
    public InvalidInputException(String s) {
        super(s);
    }
}

public class Main {

    public static void parseInputsAndProcess(String input, MySQLAccess mysqlInstance) {
        try {
            String inputPattern = "^([a-zA-Z]{3,10})\\s((\\((\\d{7}\\,?){0,2}([a-zA-Z]{3}\\,?)*\\)\\,?\\s?)+)$";
            Pattern r = Pattern.compile(inputPattern);
            Matcher m = r.matcher(input);

            if (m.find()) {
                switch (m.group(1)) {
                    case "ADD":
                        parseInputsAndAddRows(m.group(2), mysqlInstance);
                        break;
                    case "EDIT":
                        parseInputsAndEditRows(m.group(2), mysqlInstance);
                        break;
                    case "REMOVE":
                        parseInputsAndDeleteRows(m.group(2), mysqlInstance);
                        break;
                    case "VIEW":
                        parseInputsAndViewRowsByCircleCodes(m.group(2), mysqlInstance);
                        break;
                    case "VIEWRANGE":
                        parseInputsAndViewRowsByMsisdnRange(m.group(2), mysqlInstance);
                        break;
                    case "VIEWMSISDN":
                        parseInputsAndViewRowsByMsisdn(m.group(2), mysqlInstance);
                        break;
                    default:
                        throw new InvalidInputException("Invalid operation!");
                }
            } else {
                throw new InvalidInputException("Invalid operation!");
            }
        } catch (InvalidInputException | NumberFormatException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void parseInputsAndAddRows(String inputRows, MySQLAccess mysqlInstance) throws InvalidInputException, NumberFormatException {
        String [] rows = inputRows.split(", ");
        String rowPattern = "^\\((\\d{7})\\,?([a-zA-Z]{3})\\)$";
        Pattern pattern = Pattern.compile(rowPattern);
        int numberOfRows = rows.length;
        int[] msisdn_prefixes = new int[numberOfRows];
        String[] circle_short_code = new String[numberOfRows];
        for (int i = 0; i < numberOfRows; i++) {
            Matcher matcher = pattern.matcher(rows[i]);
            if (matcher.find()) {
                msisdn_prefixes[i] = Integer.parseInt(matcher.group(1));
                circle_short_code[i] = matcher.group(2);
            } else {
                throw new InvalidInputException("Invalid input!");
            }
        }

        if (mysqlInstance.addRows(msisdn_prefixes, circle_short_code) != 0) {
            System.out.println("ADDED " + numberOfRows);
        }
    }

    public static void parseInputsAndEditRows(String inputRows, MySQLAccess mysqlInstance) throws InvalidInputException, NumberFormatException {
        String [] rows = inputRows.split(", ");
        String rowPattern = "^\\((\\d{7})\\,(\\d{7})\\,([a-zA-Z]{3})\\)$";
        Pattern pattern = Pattern.compile(rowPattern);
        int numberOfRows = rows.length;
        int[] current_msisdn_prefixes = new int[numberOfRows];
        String[] updated_circle_short_code = new String[numberOfRows];
        int[] updated_msisdn_prefixes = new int[numberOfRows];
        for (int i = 0; i < numberOfRows; i++) {
            Matcher matcher = pattern.matcher(rows[i]);
            if (matcher.find()) {
                current_msisdn_prefixes[i] = Integer.parseInt(matcher.group(1));
                updated_msisdn_prefixes[i] = Integer.parseInt(matcher.group(2));
                updated_circle_short_code[i] = matcher.group(3);
            } else {
                throw new InvalidInputException("Invalid input!");
            }
        }

        if (mysqlInstance.updateRows(updated_msisdn_prefixes, updated_circle_short_code, current_msisdn_prefixes) != 0) {
            System.out.println("EDITED " + numberOfRows);
        }
    }

    public static Matcher getMatcherForInput(String inputRows, String rowPattern) throws InvalidInputException {
        String [] rows = inputRows.split(", ");
        if (rows.length != 1) {
            throw new InvalidInputException("Invalid input!");
        }
        Pattern pattern = Pattern.compile(rowPattern);
        Matcher matcher = pattern.matcher(rows[0]);

        return matcher;
    }

    public static void parseInputsAndDeleteRows(String inputRows, MySQLAccess mysqlInstance) throws InvalidInputException, NumberFormatException {
        Matcher matcher = getMatcherForInput(inputRows, "^\\((.*)\\)$");
        if(matcher.find()) {
            String [] prefixes = matcher.group(1).split(",");
            int [] msisdn_prefixes = new int[prefixes.length];
            for (int i = 0; i < prefixes.length; i++) {
                msisdn_prefixes[i] = Integer.parseInt(prefixes[i]);
            }
            if (mysqlInstance.deleteRows(msisdn_prefixes) != 0) {
                System.out.println("DELETED " + prefixes.length);
            }
        } else {
            throw new InvalidInputException("Invalid input!");
        }
    }

    public static void parseInputsAndViewRowsByCircleCodes(String inputRows, MySQLAccess mysqlInstance) throws InvalidInputException, NumberFormatException {
        Matcher matcher = getMatcherForInput(inputRows, "^\\((.*)\\)$");

        if(matcher.find()) {
            mysqlInstance.viewByCircleCode(matcher.group(1).split(","));
        } else {
            throw new InvalidInputException("Invalid input!");
        }
    }

    public static void parseInputsAndViewRowsByMsisdnRange(String inputRows, MySQLAccess mysqlInstance) throws InvalidInputException, NumberFormatException {
        Matcher matcher = getMatcherForInput(inputRows, "^\\((\\d{7})\\,?(\\d{7})\\)$");

        if(matcher.find()) {
            mysqlInstance.viewByMsisdnRange(Integer.parseInt(matcher.group(1)), Integer.parseInt(matcher.group(2)));
        } else {
            throw new InvalidInputException("Invalid input!");
        }
    }

    public static void parseInputsAndViewRowsByMsisdn(String inputRows, MySQLAccess mysqlInstance) throws InvalidInputException, NumberFormatException {
        Matcher matcher = getMatcherForInput(inputRows, "^\\((\\d{7})\\)$");

        if(matcher.find()) {
            mysqlInstance.viewByMsisdn(Integer.parseInt(matcher.group(1)));
        } else {
            throw new InvalidInputException("Invalid input!");
        }
    }

    public static void main(String[] args) {
        MySQLAccess mysqlInstance = new MySQLAccess("msisdn");
        String input;
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            System.out.print("msisdn_prefix>");
            input = reader.readLine();

            while (!input.equalsIgnoreCase("EXIT")) {
                parseInputsAndProcess(input, mysqlInstance);
                System.out.print("msisdn_prefix>");
                input = reader.readLine();
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
