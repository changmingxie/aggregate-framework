import React from "react";
import {Button, Col, message, Popconfirm, Row} from "antd";
import * as api from "../../common/api";
import {useDispatch, useSelector} from "react-redux";
import {DomainState} from "../../store/reducers/domain";
import {CommonAction} from "app-common";
import {Domain} from "../../store/actions/domain";
import {Dispatch} from "redux";

const DrawerDomainList = () => {
  const { currentDomain: domain, domainData } = useSelector<any, DomainState>(
    ({ domain }) => domain
  );
  const dispatch = useDispatch<Dispatch<CommonAction>>();

  //获取domain数据
  const getDomainList = () => {
    api.getDomains().then((res) => {
      dispatch({ type: Domain.UPDATE_DOMAIN_DATA, payload: res.data });
    });
  };

  const confirm = (value: string) => {
    api.deleteDomain(value).then((res) => {
      if (res.data.code === 200) {
        message.success("删除成功");
        getDomainList();
      } else {
        message.success("出错了，请联系管理员");
      }
    });
  };

  return (
    <>
      {domainData.map((item: any, index: number) => {
        return (
          <Row key={index} align="middle" justify="end">
            <Col span={20}>{item.value}</Col>
            <Col span={4}>
              <Popconfirm
                title={`确定要删除${item.label}吗？`}
                onConfirm={() => confirm(item.value)}
                okText="确定"
                cancelText="取消"
              >
                <Button danger type="link">
                  删除
                </Button>
              </Popconfirm>
            </Col>
          </Row>
        );
      })}
    </>
  );
};

export default DrawerDomainList;
