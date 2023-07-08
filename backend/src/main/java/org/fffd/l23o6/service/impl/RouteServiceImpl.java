package org.fffd.l23o6.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import io.github.lyc8503.spring.starter.incantation.exception.BizException;
import org.fffd.l23o6.dao.RouteDao;
import org.fffd.l23o6.dao.StationDao;
import org.fffd.l23o6.dao.TrainDao;
import org.fffd.l23o6.exception.BizError;
import org.fffd.l23o6.mapper.RouteMapper;
import org.fffd.l23o6.pojo.entity.RouteEntity;
import org.fffd.l23o6.pojo.entity.StationEntity;
import org.fffd.l23o6.pojo.entity.TrainEntity;
import org.fffd.l23o6.pojo.vo.route.RouteVO;
import org.fffd.l23o6.service.RouteService;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RouteServiceImpl implements RouteService {
    private final TrainDao trainDao;
    private final RouteDao routeDao;
    private final StationDao stationDao;
    @Override
    public void addRoute(String name, List<Long> stationIds) {
        int stationNums = stationIds.size();
        // 路线命名不合法
        validateRoute(name, stationIds, stationNums);
        // 路线已经存在
        routeExisted(-1L, stationNums, stationIds);
        // 合法 添加
        RouteEntity route = RouteEntity.builder().name(name).stationIds(stationIds).build();
        routeDao.save(route);
    }

    @Override
    public List<RouteVO> listRoutes() {
        return routeDao.findAll(Sort.by(Sort.Direction.ASC, "name")).stream().map(RouteMapper.INSTANCE::toRouteVO).collect(Collectors.toList());
    }

    @Override
    public RouteVO getRoute(Long id) {
        RouteEntity entity = routeDao.findById(id).get();
        return RouteMapper.INSTANCE.toRouteVO(entity);
    }

    @Override
    public void editRoute(Long id, String name, List<Long> stationIds) {
        int stationNums = stationIds.size();
        // 已有火车采用此路线
        routeIsUsed(id);
        //名字规范
        validateRoute(name, stationIds, stationNums);
        routeExisted(id, stationNums, stationIds);
        routeDao.save(routeDao.findById(id).get().setStationIds(stationIds).setName(name));
    }

    @Override
    public void deleteRoute(Long id) {
        // 已有火车采用此路线
        routeIsUsed(id);
        routeDao.deleteById(id);
    }

    private void routeExisted(Long id, int stationNums, List<Long> stationIds){
        List<RouteEntity> routes = routeDao.findAll();
        for (RouteEntity route : routes) {
            if (route.getId().equals(id)) continue;
            List<Long> ids = route.getStationIds();
            if (ids.size() != stationNums) continue;
            boolean same = true;
            for (int i = 0; i < stationNums; i++) {
                if (!ids.get(i).equals(stationIds.get(i))){
                    same = false;
                    break;
                }
            }
            if (same) throw new BizException(BizError.ROUTE_EXISTS);
        }
    }

    private void validateRoute(String name, List<Long> stationIds, int stationNums){
        //名字规范
        int idx = name.indexOf('-');
        if (idx == -1 || !(name.substring(0, idx).matches("^[\u4E00-\u9FA5]{2,5}$") && name.substring(idx + 1).matches("^[\u4E00-\u9FA5]{2,5}$")))
            throw new BizException(BizError.ROUTE_NAME_NOT_VALID);
        StationEntity startStation = stationDao.findById(stationIds.get(0)).get(), endStation = stationDao.findById(stationIds.get(stationNums-1)).get();
        if (!(startStation.getName().contains(name.substring(0, idx)) && endStation.getName().contains(name.substring(idx + 1))))
            throw new BizException(BizError.ROUTE_NAME_NOT_MATCH);
    }

    private void routeIsUsed(Long id){
        List<TrainEntity> trains = trainDao.findAll();
        for (TrainEntity train : trains)
            if (train.getRouteId().equals(id))
                throw new BizException(BizError.TRAIN_CAN_NOT_DELETE);
    }
}
