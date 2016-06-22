import java.io.*;
import java.util.ArrayList;

/**
 * Created by Frank Verhagen on 28-5-2016.
 * This class converts the data of rotterdamopendata_hoogtebestandtotaal_oost.csv to a suitable smaller csv file for the visualisation
 *
 */
public class GetZadkineCoordinates {
    static ArrayList<Record> dataRecords = new ArrayList<>();
    private static float NORTH;
    private static float EAST;
    private static float SOUTH;
    private static float WEST;
    private static String COMMA_DELIMITER = ",";
    private static String NEW_LINE_SEPARATOR = "\n";
    private static int size = 1000;

    public static void main(String args[]) {
        Parse();
    }

    public static void Parse() { // This method parses the data from the csv file and converts it to WGS84 coordinates
        try {

            Coordinate centerCoordinate= RDToLatLon("92796","436960");
            setMapSize(size); // Setting the size of the map. 1000 = 1000x1000m2

            String inputfile = "rotterdamopendata_hoogtebestandtotaal_oost.csv";
            FileInputStream fstream = new FileInputStream("rotterdamopendata_hoogtebestandtotaal_oost.csv");
            BufferedReader linecounter = new BufferedReader(new FileReader("rotterdamopendata_hoogtebestandtotaal_oost.csv"));
            int lines = 0;
            while (linecounter.readLine() != null) lines++;
            linecounter.close();
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String strLine;

            long count = 1;
            System.out.println("Map size is " + size + ". Start searching values in file " + inputfile);
            while ((strLine = br.readLine()) != null) {

                String[] tokens = strLine.split(",");

                try {
                    // try to parse the whole row. Only succeeds if all values are correct doubles
                    Double.parseDouble(tokens[0]);
                    Double.parseDouble(tokens[1]);
                    Double.parseDouble(tokens[2]);

                    // Create a record with a Coordinate object which contains the WGS84 format and a Z axe
                    double lat = RDToLatLon(tokens[0], tokens[1]).getLatitude();
                    double lon = RDToLatLon(tokens[0], tokens[1]).getLongitude();

                    if ( lat <=  NORTH && lat >= SOUTH && lon <= EAST && lon >= WEST) {
                        Record record = new Record(RDToLatLon(tokens[0], tokens[1]), tokens[2]);
                        dataRecords.add(record);
                        System.out.println("Added record " + tokens[0] + ", " + tokens[1]+ " to the list");
                    }

                } catch (NumberFormatException e) { // If a line is non-parseable
                    System.out.println("Line " + count+"/" + lines+" Adding record failed. Not parseable.");
                }
                count++;
            }
            in.close();

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
        FileWriter fileWriter = null;
        boolean trying = true;
        String filename = "Zadkinecoordinates.csv";
        while(trying){
            try {
                fileWriter = new FileWriter(filename);
                fileWriter.append("longitude");
                fileWriter.append(COMMA_DELIMITER);
                fileWriter.append("latitude");
                fileWriter.append(COMMA_DELIMITER);
                fileWriter.append("Z");
                fileWriter.append(NEW_LINE_SEPARATOR);
                for (int x = 0; x < dataRecords.size(); x++) {
                    fileWriter.append("" + dataRecords.get(x).getCoordinate().getLatitude());
                    fileWriter.append(COMMA_DELIMITER);
                    fileWriter.append("" + dataRecords.get(x).getCoordinate().getLongitude());
                    fileWriter.append(COMMA_DELIMITER);
                    fileWriter.append(""+ dataRecords.get(x).getZaxis());
                    fileWriter.append(NEW_LINE_SEPARATOR);
                    System.out.println("Added line " + x + "/" + dataRecords.size() + " to the file");
                }
                fileWriter.flush();
                fileWriter.close();
                trying = false;
            } catch (IOException e) {
                filename = "_" + filename;
            } finally {

            }
        }
        System.out.println("Done writing. File saved as " + filename);
    }


    private static Coordinate RDToLatLon(String x, String y) // // Method that converts Rijksdriehoekscoordinaten to WGS84 coordinaten. Also used in opdracht 1
    {
        // The city "Amsterfoort" is used as reference "Rijksdriehoek" coordinate.
        int referenceRdX = 155000;
        int referenceRdY = 463000;
        double dX = (Double.parseDouble(x) - referenceRdX) * (Math.pow(10, -5));
        double dY = (Double.parseDouble(y) - referenceRdY) * (Math.pow(10, -5));

        double sumN =
                (3235.65389 * dY) +
                        (-32.58297 * Math.pow(dX, 2)) +
                        (-0.2475 * Math.pow(dY, 2)) +
                        (-0.84978 * Math.pow(dX, 2) * dY) +
                        (-0.0655 * Math.pow(dY, 3)) +
                        (-0.01709 * Math.pow(dX, 2) * Math.pow(dY, 2)) +
                        (-0.00738 * dX) +
                        (0.0053 * Math.pow(dX, 4)) +
                        (-0.00039 * Math.pow(dX, 2) * Math.pow(dY, 3)) +
                        (0.00033 * Math.pow(dX, 4) * dY) +
                        (-0.00012 * dX * dY);
        double sumE =
                (5260.52916 * dX) +
                        (105.94684 * dX * dY) +
                        (2.45656 * dX * Math.pow(dY, 2)) +
                        (-0.81885 * Math.pow(dX, 3)) +
                        (0.05594 * dX * Math.pow(dY, 3)) +
                        (-0.05607 * Math.pow(dX, 3) * dY) +
                        (0.01199 * dY) +
                        (-0.00256 * Math.pow(dX, 3) * Math.pow(dY, 2)) +
                        (0.00128 * dX * Math.pow(dY, 4)) +
                        (0.00022 * Math.pow(dY, 2)) +
                        (-0.00022 * Math.pow(dX, 2)) +
                        (0.00026 * Math.pow(dX, 5));

        // The city "Amsterfoort" is used as reference "WGS84" coordinate.
        double referenceWgs84X = 52.15517;
        double referenceWgs84Y = 5.387206;

        double latitude = referenceWgs84X + (sumN / 3600);
        double longitude = referenceWgs84Y + (sumE / 3600);

        return new Coordinate(latitude, longitude);
    }
    private static class Coordinate {
        private double latitude;
        private double longitude;
        public double getLatitude() {
            return latitude;
        }
        public double getLongitude() {
            return longitude;
        }
        public Coordinate(double latitude, double longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
        }
    }

    private static class Record {
        private Coordinate coordinate;
        private double Zaxis;
        public Coordinate getCoordinate() {
            return coordinate;
        }
        public double getZaxis() {
            return Zaxis;
        }
        public Record(Coordinate coordinate, String Zaxis) {
            this.coordinate = coordinate;
            this.Zaxis = Double.parseDouble(Zaxis);
        }
    }
    private static void setMapSize(int size) { // Method to set the map size.
        float centerlat = 51.91764310494602f; // These are the exact LatLon coordinates of the Zadkine statue.
        float centerlon = 4.483036283475881f; //
        double latdistance;
        float londistance;
        if(size == 1000) {
            latdistance = 0.002245780; // 500m is ~0.002245780 degrees
            londistance = 0.5f;
        } else {
            latdistance = 0.002245780/2; // 250m is 0.002245780 / 2 degrees
            londistance = 0.25f;
        }
        NORTH = (float) (centerlat + (latdistance * 2));  // Calculating the north and south latitudes, depending on the latdistance
        SOUTH = (float) (centerlat - (latdistance * 2));
        WEST = (float) (centerlon + (londistance * (360 / (Math.cos(centerlat) * 400075.16)))); // Calculating the longitude depending on the latitude and the circumference of the earth
        EAST = (float) (centerlon - (londistance * (360 / (Math.cos(centerlat) * 400075.16)))); //
    }


}






