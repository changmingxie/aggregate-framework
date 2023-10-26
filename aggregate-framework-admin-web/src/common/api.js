import http from "./http";

export function getAllDomainKeys() {
  return http.get('/aggregate-framework-dashboard/api/domain/allKeys')
}

export function getAllDomains() {
  return http.get('/aggregate-framework-dashboard/api/domain/all')
}

export function domainCreate(params) {
  return http.post("/aggregate-framework-dashboard/api/domain/create", {
    ...params
  });
}

export function domainModify(params) {
  return http.post("/aggregate-framework-dashboard/api/domain/modify", {
    ...params
  });
}

export function domainDelete(params) {
  return http.post("/aggregate-framework-dashboard/api/domain/delete", {
    ...params
  });
}

export function domainAlertTest(params) {
  return http.post("/aggregate-framework-dashboard/api/domain/alertTest", {
    ...params
  });
}

export function getManageList({
                                domain,
                                xidString,
                                offset,
                                pageSize, deleted
                              }) {
  return http.post("/aggregate-framework-dashboard/api/transaction/list", {
    domain,
    xidString,
    offset,
    pageSize,
    markDeleted: deleted
  })
}

export function detail({
                        domain,
                        xidString
                              }) {
  return http.post("/aggregate-framework-dashboard/api/transaction/detail", {
    domain,
    xidString
  })
}

export function reset(params) {
  return http.post("/aggregate-framework-dashboard/api/transaction/reset", {
    domain: params.domain,
    xidString: params.xidString
  });
}

export function remove(params) {
  return http.post("/aggregate-framework-dashboard/api/transaction/markDeleted", {
    domain: params.domain,
    xidString: params.xidString
  });
}

export function restore(params) {
  return http.post("/aggregate-framework-dashboard/api/transaction/restore", {
    domain: params.domain,
    xidString: params.xidString
  });
}

export function transactionDelete(params) {
  return http.post("/aggregate-framework-dashboard/api/transaction/delete", {
    domain: params.domain,
    xidString: params.xidString
  });
}


export function getAllTask() {
  return http.get('/aggregate-framework-dashboard/api/task/all');
}

export function taskPause(domain) {
  return http.get('/aggregate-framework-dashboard/api/task/pause/' + domain);
}

export function taskResume(domain) {
  return http.get('/aggregate-framework-dashboard/api/task/resume/' + domain);
}

export function taskDelete(domain) {
  return http.get('/aggregate-framework-dashboard/api/task/delete/' + domain);
}

export function taskModifyCron(params) {
  return http.post("/aggregate-framework-dashboard/api/task/modifyCron",
    {
      domain: params.domain,
      cronExpression: params.cronExpression
    });
}


export function userLogin(params) {
  return http.post("/aggregate-framework-dashboard/api/user/login", {
    ...params
  });
}
