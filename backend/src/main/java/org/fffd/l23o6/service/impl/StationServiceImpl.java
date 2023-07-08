package org.fffd.l23o6.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.fffd.l23o6.dao.RouteDao;
import org.fffd.l23o6.dao.StationDao;
import org.fffd.l23o6.exception.BizError;
import org.fffd.l23o6.mapper.StationMapper;
import org.fffd.l23o6.pojo.entity.RouteEntity;
import org.fffd.l23o6.pojo.entity.StationEntity;
import org.fffd.l23o6.pojo.vo.station.StationVO;
import org.fffd.l23o6.service.StationService;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import io.github.lyc8503.spring.starter.incantation.exception.BizException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StationServiceImpl implements StationService{
    private final StationDao stationDao;
    private final RouteDao routeDao;
    @Override
    public StationVO getStation(Long stationId){
        return StationMapper.INSTANCE.toStationVO(stationDao.findById(stationId).get());
    }
    @Override
    public List<StationVO> listStations(){
        return stationDao.findAll(Sort.by(Sort.Direction.ASC, "name")).stream().map(StationMapper.INSTANCE::toStationVO).collect(Collectors.toList());
    }
    @Override
    public void addStation(String name){
        // 车站命名不合法
        validateStation(name);
        // 车站已经存在了
        stationExisted(name);
        stationDao.save(StationEntity.builder().name(name).build());
    }
    @Override
    public void editStation(Long id, String name){
        StationEntity entity = stationDao.findById(id).get();
        // 车站命名不合法
        validateStation(name);
        // 已有路线包含该站点
        stationIsUsed(id);
        // 同名站点已经存在
        stationExisted(name);
        // 正常可以修改
        entity.setName(name);
        stationDao.save(entity);
    }

    @Override
    public void deleteStation(Long stationId) {
        // 已有路线包含该站点
        stationIsUsed(stationId);
        stationDao.deleteById(stationId);
    }

    private void validateStation(String name){
        if (!(name.endsWith("站") && name.matches("^[\u4E00-\u9FA5]{3,6}$")))
            throw new BizException(BizError.STAION_NAME_NOT_VALID);
    }

    private void stationIsUsed(Long id){
        List<RouteEntity> routes = routeDao.findAll();
        for (RouteEntity route : routes)
            for (Long stationId : route.getStationIds())
                if (id.equals(stationId)) throw new BizException(BizError.STATION_CAN_NOT_DELETE);
    }

    private void stationExisted(String name){
        List<StationEntity> stations = stationDao.findAll();
        for (StationEntity station : stations)
            if(station.getName().equals(name))
                throw new BizException(BizError.STATIONNAME_EXISTS);
    }
}
