package org.fffd.l23o6.util.strategy.train;

import java.util.*;

import jakarta.annotation.Nullable;
import org.fffd.l23o6.pojo.enum_.TrainType;
import org.fffd.l23o6.pojo.vo.train.TicketInfo;


public class GSeriesSeatStrategy extends TrainSeatStrategy {
    public static final GSeriesSeatStrategy INSTANCE = new GSeriesSeatStrategy();

    private final Map<Integer, String> BUSINESS_SEAT_MAP = new HashMap<>();
    private final Map<Integer, String> FIRST_CLASS_SEAT_MAP = new HashMap<>();
    private final Map<Integer, String> SECOND_CLASS_SEAT_MAP = new HashMap<>();

    private final Map<GSeriesSeatType, Map<Integer, String>> TYPE_MAP = new HashMap<>() {{
        put(GSeriesSeatType.BUSINESS_SEAT, BUSINESS_SEAT_MAP);
        put(GSeriesSeatType.FIRST_CLASS_SEAT, FIRST_CLASS_SEAT_MAP);
        put(GSeriesSeatType.SECOND_CLASS_SEAT, SECOND_CLASS_SEAT_MAP);
    }};


    private GSeriesSeatStrategy() {

        int counter = 0;

        for (String s : Arrays.asList("1车1A","1车1C","1车1F")) {
            BUSINESS_SEAT_MAP.put(counter++, s);
        }

        for (String s : Arrays.asList("2车1A","2车1C","2车1D","2车1F","2车2A","2车2C","2车2D","2车2F","3车1A","3车1C","3车1D","3车1F")) {
            FIRST_CLASS_SEAT_MAP.put(counter++, s);
        }

        for (String s : Arrays.asList("4车1A","4车1B","4车1C","4车1D","4车2F","4车2A","4车2B","4车2C","4车2D","4车2F","4车3A","4车3B","4车3C","4车3D","4车3F")) {
            SECOND_CLASS_SEAT_MAP.put(counter++, s);
        }
        
    }

    public enum GSeriesSeatType implements SeatType {
        BUSINESS_SEAT("商务座"), FIRST_CLASS_SEAT("一等座"), SECOND_CLASS_SEAT("二等座"), NO_SEAT("无座");
        private String text;
        GSeriesSeatType(String text){
            this.text=text;
        }
        public String getText() {
            return this.text;
        }
        public static GSeriesSeatType fromString(String text) {
            for (GSeriesSeatType b : GSeriesSeatType.values()) {
                if (b.text.equalsIgnoreCase(text)) {
                    return b;
                }
            }
            return null;
        }
    }

    //TODO
    public double getPrice(int startStationIndex, int endStationIndex, GSeriesSeatType type){
        double basePrice = 50*(endStationIndex - startStationIndex);
        switch (type){
            case BUSINESS_SEAT: return 2*basePrice;
            case FIRST_CLASS_SEAT: return 1.5*basePrice;
            default: return basePrice;
        }
    }

    //TODO
    public int[] getStartEndOfSeatMap(GSeriesSeatType type){
        int[] start_end = new int[2];
        switch (type){
            case BUSINESS_SEAT: 
                start_end[0] = 0;
                start_end[1] = BUSINESS_SEAT_MAP.size();
                break;
            case FIRST_CLASS_SEAT: 
                start_end[0] = BUSINESS_SEAT_MAP.size();
                start_end[1] = BUSINESS_SEAT_MAP.size() + FIRST_CLASS_SEAT_MAP.size();
                break;
            default: 
                start_end[0] = BUSINESS_SEAT_MAP.size() + FIRST_CLASS_SEAT_MAP.size();
                start_end[1] = BUSINESS_SEAT_MAP.size() + FIRST_CLASS_SEAT_MAP.size() + SECOND_CLASS_SEAT_MAP.size();
        }
        return start_end;
    }

    public @Nullable String allocSeat(int startStationIndex, int endStationIndex, GSeriesSeatType type, boolean[][] seatMap) {
        // TODO
        int[] start_end = getStartEndOfSeatMap(type);
        for (int i = start_end[0]; i < start_end[1]; i++) {
            boolean available = true;
            //endStationIndex - 1 = upper bound  => 最后一站下车，不用管是否有人
            for (int j = startStationIndex; j < endStationIndex; j++)
                if (!seatMap[j][i])
                    available = false;
            //若该座位一直空闲，设置为有人，返回该座位
            if (available) {
                for (int j = startStationIndex; j < endStationIndex; j++)
                    seatMap[j][i] = false;
                //座位类型=>座位号
                return TYPE_MAP.get(type).get(i); //注意：每一个map的首个元素序号是从上一个map的末尾开始，具体原因见GSeriesSeatStrategy()
            }
        }
        return null;
    }

    // TODO
    public void returnSeat(int startStationIndex, int endStationIndex, String seatVal, boolean[][] seatMap) {
        for (Map<Integer, String> seats : TYPE_MAP.values()) {
            for (var seat : seats.entrySet()) {
                if (seat.getValue().equals(seatVal)){
                    int i = seat.getKey();
                    for (int j = startStationIndex; j < endStationIndex; j++)
                        seatMap[j][i] = true;
                    return;
                }
            }
        }
    }

    public List<TicketInfo> getTicketInfos(int startStationIndex, int endStationIndex, boolean[][] seatMap) {
        // TODO
        List<TicketInfo> ticketInfos = new ArrayList<>();
        for (GSeriesSeatType type : TYPE_MAP.keySet()) {
            int count = 0;
            int[] start_end = getStartEndOfSeatMap(type);
            for (int i = start_end[0]; i < start_end[1]; i++) {
                boolean available = true;
                //endStationIndex - 1 = upper bound  => 最后一站下车，不用管是否有人
                for (int j = startStationIndex; j < endStationIndex; j++)
                    if (!seatMap[j][i])
                        available = false;
                //若该座位一直空闲，count++
                if (available) count++;
            }
            int price = (int)getPrice(startStationIndex, endStationIndex, type);
            ticketInfos.add(new TicketInfo(type.getText(), count, price));
        }
        return ticketInfos;
    }

    public boolean[][] initSeatMap(int stationCount) {
        boolean[][] seatMap = new boolean[stationCount - 1][BUSINESS_SEAT_MAP.size() + FIRST_CLASS_SEAT_MAP.size() + SECOND_CLASS_SEAT_MAP.size()];
        for (int i = 0; i < seatMap.length; i++)
            for (int j = 0; j < seatMap[0].length; j++)
                seatMap[i][j] = true;
        return seatMap;
    }
}
