package fi.academy;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;

import java.text.DateFormat;
import java.util.Locale;

import java.net.URI;
import java.net.URL;
import java.util.*;

public class Main {
    static HashMap<String, String> pitkanimiLyhytnimi = new HashMap<>();
    static HashMap<String, String> lyhytnimiPitkanimi = new HashMap<>();

    public static void main(String[] args) {
        lueAsemanJSONData();

        System.out.println("Tervetuloa");

        lueJunanJSONData();
    }

    public static void lueJunanJSONData() {
        String baseurl = "https://rata.digitraffic.fi/api/v1";
        try {
            Scanner lukija = new Scanner(System.in);
            System.out.println("Anna lähtöasema (esim. Helsinki): ");
            String departureStation = (lukija.nextLine());
            String departureStationPlus = departureStation + " asema";
            String departureShort;

            if (pitkanimiLyhytnimi.get(departureStation) == null) {
                departureShort = pitkanimiLyhytnimi.get(departureStationPlus);
            } else {
                departureShort = pitkanimiLyhytnimi.get(departureStationPlus);
            }

            System.out.println("Anna määränpää (esim. Kouvola): ");
            String destinationStation = (lukija.nextLine());
            String destinationStationPlus = destinationStation + " asema";
            String destinationShort;

            if (pitkanimiLyhytnimi.get(destinationStation) == null) {
                destinationShort = pitkanimiLyhytnimi.get(destinationStationPlus);
            } else {
                destinationShort = pitkanimiLyhytnimi.get(destinationStationPlus);
            }

            URL url = new URL(URI.create(String.format("%s/live-trains/station/" + departureShort + "/" + destinationShort, baseurl)).toASCIIString());
            ObjectMapper mapper = new ObjectMapper();
            CollectionType tarkempiListanTyyppi = mapper.getTypeFactory().constructCollectionType(ArrayList.class, Juna.class);
            List<Juna> junat = mapper.readValue(url, tarkempiListanTyyppi);  // pelkkä List.class ei riitä tyypiksi
            StringBuilder sb = new StringBuilder();
            for (Juna j : junat) {
                String junanTyyppi = j.getTrainType();
                String junanNumero = j.getTrainNumber();
                sb.append("Juna " + junanTyyppi);
                sb.append(junanNumero);
                for (TimeTableRow ttr : j.timeTableRows) {
                    if (ttr.getStationShortCode().equals(departureShort) && ttr.getType().equals("DEPARTURE")) {
                        sb.append(" lähtee asemalta ")
                                .append(departureStation)
                                .append(": ")
                                .append(ttr.getScheduledTime())
                                .append("\n");
                    }
                    if (ttr.getStationShortCode().equals(destinationShort) && ttr.getType().equals("ARRIVAL")) {
                        sb.append("Juna saapuu asemalle ")
                                .append(destinationStation)
                                .append(": ")
                                .append(ttr.getScheduledTime())
                                .append("\n \n");
                    }
                }
            }
            System.out.println(sb);

        } catch (Exception ex) {
            System.out.println("Tapahtui VIRHE!");
        }
    }

    public static void lueAsemanJSONData() {
        String baseurl = "https://rata.digitraffic.fi/api/v1";

        try {
            URL url = new URL(URI.create(String.format("%s/metadata/stations", baseurl)).toASCIIString());
            ObjectMapper mapper = new ObjectMapper();
            CollectionType asemaLista = mapper.getTypeFactory().constructCollectionType(ArrayList.class, Asema.class);
            List<Asema> assat = mapper.readValue(url, asemaLista);

            for (int i = 0; i < assat.size(); i++) {
                if (assat.get(i).isPassengerTraffic()) {
                    String longName = assat.get(i).getStationName();
                    String shortName = assat.get(i).getStationShortCode();
                    pitkanimiLyhytnimi.put(longName, shortName);
                    lyhytnimiPitkanimi.put(shortName, longName);
                }
            }

        } catch (Exception ex) {
            System.out.println("Tapahtui VIRHE");
        }
    }
}

@JsonIgnoreProperties(ignoreUnknown = true)
class Juna {
    //LocalDate departureDate;  // Jackson ei oikein pidä Java 8 päivistä oletuksena
    List<TimeTableRow> timeTableRows;

    String trainType;
    String trainNumber;
    long version;
    List<Asema> assat;

    @Override
    public String toString() {
        return "Juna{" + '\'' + ", operatorShortCode='" + ", timeTableRows=" + timeTableRows + '\'' + ", trainType='" + trainType + '\'' + ", version=" + version + '}';
    }
    public String getTrainType() {
        return trainType;
    }
    public String getTrainNumber() {
        return trainNumber;
    }
    public List<Asema> getAssat() {
        return assat;
    }
    public List<TimeTableRow> getTimeTableRows() {
        return timeTableRows;
    }
    public void setTimeTableRows(List<TimeTableRow> timeTableRows) {
        this.timeTableRows = timeTableRows;
    }
}

@JsonIgnoreProperties(ignoreUnknown = true)
class TimeTableRow {
    Date scheduledTime;
    String stationShortCode;
    String type;

    public String getStationShortCode() {
        return stationShortCode;
    }

    public String getType() {
        return type;
    }

    public String getScheduledTime() {
        return DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, new Locale("FI", "fi")).format(scheduledTime);
    }

    @Override
    public String toString() {
        return "Arrival time to " + getStationShortCode() + ": " + getScheduledTime();
    }
}
@JsonIgnoreProperties(ignoreUnknown = true)
class Asema {
        String stationName;
        String stationShortCode;
        boolean passengerTraffic;

        public boolean isPassengerTraffic() {
           return passengerTraffic;
        }

        public String getStationShortCode() {
            return stationShortCode;
        }
        public String getStationName() {
            return stationName;
        }
        @Override
        public String toString() {
            return stationName + ", " + stationShortCode;
        }
}

