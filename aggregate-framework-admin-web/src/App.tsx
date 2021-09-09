import React from 'react';
import {Provider} from 'react-redux';
import {Layout, Menu} from 'antd';
import {BrowserRouter as Router, Link, Route, Switch} from 'react-router-dom';
import './App.css';
import Domain from './pages/domain/index';
import Degrade from './pages/Degrade';
import store from './store';

const {Header, Content} = Layout;

const App = () => (
  <Provider store={store}>
    <Router basename="/gatekeeper/business-aggregate-admin-web">
      <Layout className="layout">
        <Header
          style={{
            backgroundColor: '#fff',
            fontSize: 18,
            fontWeight: 'bold'
          }}
        >
          <div style={{float: 'left', marginRight: 80}}>AGG 管理后台</div>
          <Menu mode="horizontal">
            <Menu.Item>
              <Link to="/normal">常规</Link>
            </Menu.Item>
            <Menu.Item>
              <Link to="/degrade">降级配置</Link>
            </Menu.Item>
          </Menu>
        </Header>
        <Content>
          <div className="site-layout-content">
            <Switch>
              <Route path="/normal">
                <Domain/>
              </Route>
              <Route path="/degrade">
                <Degrade/>
              </Route>
            </Switch>
          </div>
        </Content>
      </Layout>
    </Router>
  </Provider>
);

export default App;
