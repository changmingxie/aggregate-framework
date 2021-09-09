import React from "react";
import ReactDOM from "react-dom";
import {ConfigProvider} from "antd";
import zhCN from "antd/lib/locale-provider/zh_CN";
import "antd/dist/antd.css";
import App from "./App";

ReactDOM.render(
  <ConfigProvider locale={zhCN}>
    <App />
  </ConfigProvider>,
  document.getElementById("root")
);
