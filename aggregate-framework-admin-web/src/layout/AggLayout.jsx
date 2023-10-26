import React, {useEffect, useState} from "react";

import AggHeader from "./AggHeader";
import {Layout, Menu} from "antd";
import {Link, withRouter} from "react-router-dom";

const {Content, Sider} = Layout;


const Page = (props) => {
  const {history} = props;
  const {routeList} = props;
  const [current, setCurrent] = useState(null);
  let connectionMode = localStorage.getItem('connectionMode');
  const isServerMode = connectionMode && connectionMode.toUpperCase() === 'SERVER';
  const onClick = e => {
    setCurrent(e.key);
  };
  useEffect(() => {
    let url = window.location.href;
    console.log('app', url)
    let lastIndex = url.lastIndexOf("/");
    let key = 'welcome';
    if (!url.endsWith("/")) {
      key = url.substring(lastIndex + 1)
    }
    history.push('/' + key);
    setCurrent(key);
  }, []);

  return (
    <>
      <Layout className="layout">
        <AggHeader/>
        <Layout>
          <Sider>
            <Menu
              selectedKeys={[current]}
              defaultSelectedKeys={['welcome']}
              theme="dark"
              onClick={onClick}>
              <Menu.Item key='welcome'>
                <Link to="/welcome">首页</Link>
              </Menu.Item>
              <Menu.Item key='domain'>
                <Link to="/domain">Domain管理</Link>
              </Menu.Item>
              <Menu.Item key='transaction'>
                <Link to="/transaction">事件管理</Link>
              </Menu.Item>
              {
                isServerMode ?
                  <Menu.Item key='task'>
                    <Link to="/task">任务管理</Link>
                  </Menu.Item>
                  :
                  <></>
              }
            </Menu>
          </Sider>
          <Content>
            <div className="site-layout-content">
              {routeList}
            </div>
          </Content>
        </Layout>
      </Layout>
    </>
  );
}

export default withRouter(Page);
