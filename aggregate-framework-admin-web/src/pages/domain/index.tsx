import React, {useEffect, useState} from 'react';
import {Tabs} from 'antd';
import * as api from '../../common/api';
import SearchBox from './SearchBox';
import {useDispatch, useSelector} from 'react-redux';
import {DomainState} from '../../store/reducers/domain';
import {CommonAction} from 'app-common';
import {Domain} from '../../store/actions/domain';
import {Dispatch} from 'redux';
import TableCard from './TableCard';

const {TabPane} = Tabs;

const Page = () => {
  const [activeTabKey, setActiveTabKey] = useState('normal');
  const {currentDomain: domain, domainData} = useSelector<any, DomainState>(({domain}) => domain);
  const dispatch = useDispatch<Dispatch<CommonAction>>();

  //获取domain数据
  const getDomainList = () => {
    api.getDomains().then((res) => {
      dispatch({type: Domain.UPDATE_DOMAIN_DATA, payload: res.data});
    });
  };

  useEffect(() => {
    getDomainList();
  }, []);

  const onTabChange = (tab: string) => {
    setActiveTabKey(tab);
  };

  return (
    <React.Fragment>
      <SearchBox/>
      <div className="content">
        <Tabs
          defaultActiveKey="normal"
          onChange={onTabChange}
          activeKey={activeTabKey}
        >
          <TabPane tab="Normal" key="normal"/>
          <TabPane tab="Deleted Keys" key="deletedKeys"/>
        </Tabs>
        {domainData.length && domainData
          .find((val: any) => val.value === domain)
          ?.children
          .map((row: any) => {
            return (
              <TableCard key={domain.concat(row.value)} row={row.value} activeTabKey={activeTabKey}/>
            );
          })}
      </div>
    </React.Fragment>
  );
};

export default Page;
