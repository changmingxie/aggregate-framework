import {Button, Modal} from 'antd';
import ReactJson from 'react-json-view';
import React from 'react';
import {ColumnsType} from 'antd/es/table';

const {info} = Modal;

const columns: ColumnsType<any> = [
  {
    title: 'Domain',
    key: 'domain',
    dataIndex: 'domain',
    // render: (text: any) => <span>{domain}</span>,
  },
  {
    title: 'ID',
    key: 'key',
    dataIndex: 'key',
  },
  {
    title: 'Status',
    key: 'status',
    dataIndex: 'status',
  },
  {
    title: 'Transaction Type',
    key: 'type',
    dataIndex: 'type',
  },
  {
    title: 'Retried Count',
    key: 'retried',
    dataIndex: 'retried',
  },
  {
    title: 'Content',
    key: 'content',
    dataIndex: 'content',
    render: (text: string) => {
      return (
        <Button
          className="button"
          size="small"
          type="primary"
          onClick={() => {
            info({
              content: <ReactJson src={JSON.parse(text)}/>,
              width: '90%',
            });
          }}
        >
          查看详情
        </Button>
      );
    },
  },
  {
    title: 'Create Time',
    key: 'createTime',
    dataIndex: 'createTime',
  },
  {
    title: 'Last Update Time',
    key: 'lastUpdateTime',
    dataIndex: 'lastUpdateTime',
  },
];

export default columns;
