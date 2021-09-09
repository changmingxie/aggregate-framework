import axios from 'axios';
// @ts-ignore
import {addData} from './interface';

/**
 * 获取所有domains
 */
export function getDomains() {
  return axios({
    url: `/business-aggregate-admin/api/domains`,
    method: 'get',
  });
}

/**
 * 获取列表  domain=test&pagenum=1
 */
export function getList(data: any) {
  return axios({
    url: `/business-aggregate-admin/api/manage`,
    method: 'get',
    params: data,
  });
}

// 删除domain
export function deleteDomain(domainName: string) {
  const url = `/business-aggregate-admin/api/domain?domain=${domainName}`;
  return axios.delete(url);
}

/**
 * 重置
 */
export function handleToReset(data: any) {
  return axios({
    url: `/business-aggregate-admin/api/reset`,
    method: 'put',
    data: data,
  });
}

/**
 * 新增
 */
export function handleToAdd(data: addData) {
  return axios({
    url: `/business-aggregate-admin/api/domain`,
    method: 'post',
    data: data,
  });
}

export function getDegradeList() {
  return axios
    .get('/business-aggregate-admin/api/degrade')
    .then(res => res.data.data);
}

export function degrade(domain: string, isDegrade: boolean) {
  const url = `/business-aggregate-admin/api/degrade?domain=${domain}&degrade=${isDegrade}`;
  return axios.put(url);
}

export function deleteKey({domain, row, keys}: any) {
  return axios.delete(`/business-aggregate-admin/api/key?domain=${domain}&row=${row}&keys=${keys}`);
}

export function restore({domain, row, keys}: any) {
  return axios.put(`/business-aggregate-admin/api/key/restore?domain=${domain}&row=${row}&keys=${keys}`);
}
