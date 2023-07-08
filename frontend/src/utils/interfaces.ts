interface OrderDetailData {
  id: number,
  train_id: number,
  seat: string,
  status: string,
  created_at: string,
  start_station_id: number,
  end_station_id: number,
  departure_time: string,
  arrival_time: string,
  //TODO
  price: number,
  balance: number,
  payment_strategy: string,
  bonus_points: number,
  consumption_points: number,
  left_points: number,
  use_points: boolean,

}

interface TicketInfo {
  type: string,
  price: number,
  count: number,
}

interface RouteInfo {
  id: number,
  name: string,
  station_ids: Array<number>,
}

interface StationInfo {
  id: number,
  name: string,
}

interface TrainInfo {
  id: number,
  name: string,
  route_id: number,
  train_type: string,
  date: string,
  departure_times: Array<string>,
  arrival_times: Array<string>,
  extra_infos: Array<string>
}

interface TrainDetailInfo {
  id: number,
  name: string,
  station_ids: Array<number>,
  date: string,
  departure_times: Array<string>,
  arrival_times: Array<string>,
  extra_infos: Array<string>
}

export { OrderDetailData, TicketInfo, RouteInfo, StationInfo, TrainInfo,TrainDetailInfo }