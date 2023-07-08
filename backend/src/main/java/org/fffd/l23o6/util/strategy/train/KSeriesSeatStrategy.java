package org.fffd.l23o6.util.strategy.train;

import java.util.*;

import jakarta.annotation.Nullable;
import org.fffd.l23o6.pojo.vo.train.TicketInfo;


public class KSeriesSeatStrategy extends TrainSeatStrategy {
    public static final KSeriesSeatStrategy INSTANCE = new KSeriesSeatStrategy();
     
    private final Map<Integer, String> SOFT_SLEEPER_SEAT_MAP = new HashMap<>();
    private final Map<Integer, String> HARD_SLEEPER_SEAT_MAP = new HashMap<>();
    private final Map<Integer, String> SOFT_SEAT_MAP = new HashMap<>();
    private final Map<Integer, String> HARD_SEAT_MAP = new HashMap<>();

    private final Map<KSeriesSeatType, Map<Integer, String>> TYPE_MAP = new HashMap<>() {{
        put(KSeriesSeatType.SOFT_SLEEPER_SEAT, SOFT_SLEEPER_SEAT_MAP);
        put(KSeriesSeatType.HARD_SLEEPER_SEAT, HARD_SLEEPER_SEAT_MAP);
        put(KSeriesSeatType.SOFT_SEAT, SOFT_SEAT_MAP);
        put(KSeriesSeatType.HARD_SEAT, HARD_SEAT_MAP);
    }};


    private KSeriesSeatStrategy() {

        int counter = 0;

        for (String s : Arrays.asList("软卧1号上铺", "软卧2号下铺", "软卧3号上铺", "软卧4号上铺", "软卧5号上铺", "软卧6号下铺", "软卧7号上铺", "软卧8号上铺")) {
            SOFT_SLEEPER_SEAT_MAP.put(counter++, s);
        }

        for (String s : Arrays.asList("硬卧1号上铺", "硬卧2号中铺", "硬卧3号下铺", "硬卧4号上铺", "硬卧5号中铺", "硬卧6号下铺", "硬卧7号上铺", "硬卧8号中铺", "硬卧9号下铺", "硬卧10号上铺", "硬卧11号中铺", "硬卧12号下铺")) {
            HARD_SLEEPER_SEAT_MAP.put(counter++, s);
        }

        for (String s : Arrays.asList("1车1座", "1车2座", "1车3座", "1车4座", "1车5座", "1车6座", "1车7座", "1车8座", "2车1座", "2车2座", "2车3座", "2车4座", "2车5座", "2车6座", "2车7座", "2车8座")) {
            SOFT_SEAT_MAP.put(counter++, s);
        }

        for (String s : Arrays.asList("3车1座", "3车2座", "3车3座", "3车4座", "3车5座", "3车6座", "3车7座", "3车8座", "3车9座", "3车10座", "4车1座", "4车2座", "4车3座", "4车4座", "4车5座", "4车6座", "4车7座", "4车8座", "4车9座", "4车10座")) {
            HARD_SEAT_MAP.put(counter++, s);
        }
    }

    public enum KSeriesSeatType implements SeatType {
        SOFT_SLEEPER_SEAT("软卧"), HARD_SLEEPER_SEAT("硬卧"), SOFT_SEAT("软座"), HARD_SEAT("硬座"), NO_SEAT("无座");
        private String text;
        KSeriesSeatType(String text){
            this.text=text;
        }
        public String getText() {
            return this.text;
        }
        public static KSeriesSeatType fromString(String text) {
            for (KSeriesSeatType b : KSeriesSeatType.values()) {
                if (b.text.equalsIgnoreCase(text)) {
                    return b;
                }
            }
            return null;
        }
    }

    //TODO
    public double getPrice(int startStationIndex, int endStationIndex, KSeriesSeatType type){
        double basePrice = 50*(endStationIndex - startStationIndex);
        switch (type){
            case SOFT_SLEEPER_SEAT: return 2.5*basePrice;
            case HARD_SLEEPER_SEAT: return 2*basePrice;
            case SOFT_SEAT: return 1.5*basePrice;
            default: return basePrice;
        }
    }

    //TODO
    public int[] getStartEndOfSeatMap(KSeriesSeatType type){
        int[] start_end = new int[2];
        switch (type){
            case SOFT_SLEEPER_SEAT:
                start_end[0] = 0;
                start_end[1] = SOFT_SLEEPER_SEAT_MAP.size();
                break;
            case HARD_SLEEPER_SEAT:
                start_end[0] = SOFT_SLEEPER_SEAT_MAP.size();
                start_end[1] = SOFT_SLEEPER_SEAT_MAP.size() + HARD_SLEEPER_SEAT_MAP.size();
                break;
            case SOFT_SEAT:
                start_end[0] = SOFT_SLEEPER_SEAT_MAP.size() + HARD_SLEEPER_SEAT_MAP.size();
                start_end[1] = SOFT_SLEEPER_SEAT_MAP.size() + HARD_SLEEPER_SEAT_MAP.size() + SOFT_SEAT_MAP.size();
                break;
            default:
                start_end[0] = SOFT_SLEEPER_SEAT_MAP.size() + HARD_SLEEPER_SEAT_MAP.size() + SOFT_SEAT_MAP.size();
                start_end[1] = SOFT_SLEEPER_SEAT_MAP.size() + HARD_SLEEPER_SEAT_MAP.size() + SOFT_SEAT_MAP.size() + HARD_SEAT_MAP.size();
        }
        return start_end;
    }

    public @Nullable String allocSeat(int startStationIndex, int endStationIndex, KSeriesSeatType type, boolean[][] seatMap) {
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
                return TYPE_MAP.get(type).get(i); //注意：每一个map的首个元素序号是从上一个map的末尾开始，具体原因见KSeriesSeatStrategy()
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
        for (KSeriesSeatType type : TYPE_MAP.keySet()) {
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
        boolean[][] seatMap =  new boolean[stationCount - 1][SOFT_SLEEPER_SEAT_MAP.size() + HARD_SLEEPER_SEAT_MAP.size() + SOFT_SEAT_MAP.size() + HARD_SEAT_MAP.size()];
        for (int i = 0; i < seatMap.length; i++)
            for (int j = 0; j < seatMap[0].length; j++)
                seatMap[i][j] = true;
        return seatMap;
    }
}
