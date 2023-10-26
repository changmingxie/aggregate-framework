import React from 'react';
import {Provider} from 'react-redux';
import {HashRouter as Router, Redirect, Route, Switch} from 'react-router-dom';
import 'antd/dist/antd.css';
import './App.css';

import Login from './pages/agg/login/index';
import Welcome from './pages/agg/welcome/index';
import Domain from './pages/agg/domain/index';
import Transaction from './pages/agg/transaction/index';
import Task from "./pages/agg/task/index";

import store from './store';
import AggLayout from "./layout/AggLayout";

function App(props) {
  return (
    <Provider store={store}>
      <Router forceRefresh={false}>
        <Switch>
          <Route key="login" path="/login" component={Login}></Route>
          <AggLayout routeList={
            <>
              <Route path="/welcome" component={Welcome}></Route>
              <Route path="/domain" component={Domain}></Route>
              <Route path="/transaction" component={Transaction}></Route>
              <Route path="/task" component={Task}></Route>
            </>
          }>
          </AggLayout>
          <Redirect to="/welcome" from="/"/>
        </Switch>
      </Router>
    </Provider>
  );

}

export default App;
