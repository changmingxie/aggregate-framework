import React, {FC, useEffect, useState} from 'react';
import {Button, Card, message, Space, Table} from 'antd';
import columns from './Columns';
import {useSelector} from 'react-redux';
import {PaginationProps} from "antd/lib/pagination";
import {DomainState} from '../../store/reducers/domain';
import * as api from '../../common/api';

interface TableCardProps {
  row: string;
  activeTabKey: string;
}

const TableCard: FC<TableCardProps> = (props) => {
  const {row, activeTabKey} = props;
  const {currentDomain: domain, refresh} = useSelector<any, DomainState>(({domain}) => domain);
  const [selectedRowKeys, setselectedRowKeys] = useState([]);
  const [datasource, setdatasource] = useState<any>({}); //列表数据
  const [loadingStatus, setloadingStatus] = useState<boolean>(false); //加载数据
  const isDeleted = activeTabKey === 'deletedKeys';
  const [pagination, setPagination] = useState<PaginationProps>({
    current: 1,
    pageSize: 10,
    total: 1,
  });
  const handleTableChange = (pagination: PaginationProps) => {
    setPagination({
      current: pagination.current,
      pageSize: pagination.pageSize,
      total: pagination.total,
    });
  };
  //获取列表数据
  const getListData = (pagination: PaginationProps) => {
    setloadingStatus(true);
    setdatasource([]);
    let data = {
      domain,
      pageNum: pagination.current,
      pageSize: pagination.pageSize,
      row,
      isDeleted
    };
    api
      .getList(data)
      .then((res) => {
        setloadingStatus(false);
        setdatasource(res.data);
        setPagination({
          current: res.data?.pageNum,
          pageSize: res.data?.pageSize,
          total: res.data?.total,
        })
      })
      .catch((res) => {
        setloadingStatus(false);
        if (res.response.status === 500) {
          message.error('服务异常，请稍后再试');
        }
      });
  };

  const rowSelection = {
    selectedRowKeys,
    onChange: (rowKeys: any) => {
      setselectedRowKeys(rowKeys);
    }
  };

  const handleDelete = () => {
    api.deleteKey({
      domain: domain,
      row: row,
      keys: selectedRowKeys.toString()
    }).then((res) => {
      if (+res.data.code === 200) {
        getListData(pagination);
        setselectedRowKeys([]);
        message.success('操作成功');
      } else {
        message.error(res.data.message || '操作失败');
      }
    }).catch((res) => {
      if (res.response.status === 500) {
        message.error('服务异常，请稍后再试');
      }
    });
  };

  const handleReset = () => {
    const data = {
      domain: domain,
      row: row,
      keys: selectedRowKeys,
    };
    api
      .handleToReset(data)
      .then((res) => {
        if (+res.data.code === 200) {
          getListData(pagination);
          setselectedRowKeys([]);
          message.success('重置成功');
        } else {
          message.error(res.data.message);
        }
      })
      .catch((res) => {
        if (res.response.status === 500) {
          message.error('服务异常，请稍后再试');
        }
      });
  };

  useEffect(() => {
    getListData(pagination);
  }, [activeTabKey, refresh, pagination.current, pagination.pageSize]);


  return isDeleted
    ? (
      <Card title={domain.concat(row)}>
        <Table
          rowKey="key"
          columns={columns.concat({
            title: 'Operation',
            key: 'operation',
            dataIndex: 'operation',
            render(text, record) {
              return <Space>
                <Button type="link" onClick={() => {
                  api.restore({
                    domain,
                    row,
                    keys: record.key
                  }).then((res) => {
                    if (+res.data.code === 200) {
                      message.success('操作成功');
                      getListData(pagination);
                    } else {
                      message.error(res.data.message || '操作失败');
                    }
                  }).catch((res) => {
                    if (res.response.status === 500) {
                      message.error('服务异常，请稍后再试');
                    }
                  });
                }}>restore</Button>
                <Button type="link" onClick={() => {
                  api.deleteKey({
                    domain,
                    row,
                    keys: record.key
                  }).then((res) => {
                    if (+res.data.code === 200) {
                      message.success('操作成功');
                      getListData(pagination);
                    } else {
                      message.error(res.data.message || '操作失败');
                    }
                  }).catch((res) => {
                    if (res.response.status === 500) {
                      message.error('服务异常，请稍后再试');
                    }
                  });
                }} style={{color: 'red'}}>delete</Button>
              </Space>;
            },
          })}
          dataSource={datasource.items}
          size="small"
          bordered
          loading={loadingStatus}
          pagination={pagination}
          onChange={handleTableChange}
        />
      </Card>
    )
    : (
      <Card title={domain.concat(row)}>
        <div style={{margin: '30px 0'}}>
          <Space>
            <Button
              type="primary"
              disabled={!selectedRowKeys.length}
              onClick={handleReset}
            >
              重置
            </Button>
            <Button type="danger" disabled={!selectedRowKeys.length} onClick={handleDelete}>
              批量删除
            </Button>
          </Space>
        </div>
        <Table
          rowKey="key"
          columns={columns}
          dataSource={datasource.items}
          size="small"
          bordered
          rowSelection={rowSelection}
          loading={loadingStatus}
          pagination={pagination}
          onChange={handleTableChange}
        />
      </Card>
    );
};

export default TableCard;
