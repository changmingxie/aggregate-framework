package org.aggregateframework.transaction.server.registration;

import org.aggregateframework.ha.registry.Registration;
import org.aggregateframework.transaction.server.dao.Daos;
import org.aggregateframework.transaction.server.dao.TransactionDao;
import org.aggregateframework.transaction.server.model.Page;
import org.aggregateframework.transaction.server.model.ResetRequest;
import org.aggregateframework.transaction.server.model.Result;
import org.aggregateframework.transaction.server.model.Transaction;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.function.Function;

/**
 * Created by Lee on 2020/4/8 18:28.
 */
@RestController
@RequestMapping("/api")
public class RegistrationController {

    private final RegistrationContainer container;

    private final Daos daos;

    private static final int DEFAULT_PAGE_SIZE = 10;

    public RegistrationController(RegistrationContainer container, Daos daos) {
        this.container = container;
        this.daos = daos;
    }

    /**
     * 获得所有domain
     */
    @GetMapping("/domains")
    public List<String> domains() {
        return daos.domains();
    }

    /**
     * 获取指定domain的明细(实例列表)
     */
    @GetMapping("/domain/detail")
    public Daos.Cascader domainDetail(@RequestParam String domain){
        return daos.obtainDomainDetail(domain);
    }


    @GetMapping("/manage")
    public Object list(@RequestParam String domain,
                       @RequestParam String row,
                       @RequestParam(required = false, defaultValue = "false") boolean isDeleted,
                       @RequestParam(required = false, defaultValue = "1") Integer pageNum,
                       @RequestParam(required = false, defaultValue = "" + DEFAULT_PAGE_SIZE) Integer pageSize) {

        return daos.get(domain, row)
                .map(new Function<TransactionDao, Page<Transaction>>() {
                    @Override
                    public Page<Transaction> apply(TransactionDao dao) {

                        int tc = isDeleted ? dao.countDeleted() : dao.count();
                        if (tc == 0L) {
                            return Page.empty();
                        } else {
                            int pages = (tc / pageSize);
                            List<Transaction> vos = isDeleted ?
                                    dao.findDeleted(pageNum, pageSize) : dao.find(pageNum, pageSize);
                            if (tc % pageSize > 0) {
                                pages++;
                            }

                            Page<Transaction> page = new Page<>();
                            page.setItems(vos);
                            page.setPageNum(pageNum);
                            page.setPages(pages);
                            page.setPageSize(pageSize);
                            page.setTotal(tc);
                            return page;
                        }
                    }
                }).orElse(Page.empty());


    }


    /**
     * 重置
     *
     * @param resetRequest
     * @return
     */
    @PutMapping("/reset")
    public Object reset(@RequestBody ResetRequest resetRequest) {
        return daos.get(resetRequest.getDomain(), resetRequest.getRow())
                .map(dao -> {
                    try {
                        dao.reset(resetRequest.getKeys());
                        return Result.ok();
                    } catch (Exception err) {
                        return Result.err(err);
                    }
                }).orElse(Result.ok());


    }

    /**
     * 软删除key
     */
    @DeleteMapping("/key")
    public Object removeKey(@RequestParam String domain, @RequestParam String row, @RequestParam List<String> keys) {
        return daos.get(domain, row)
                .map(dao -> {
                    try {
                        dao.delete(keys);
                        return Result.ok();
                    } catch (Exception err) {
                        return Result.err(err);
                    }
                }).orElse(Result.ok());
    }

    /**
     * 恢复key
     */
    @PutMapping("/key/restore")
    public Object restoreKey(@RequestParam String domain, @RequestParam String row, @RequestParam List<String> keys) {
        return daos.get(domain, row)
                .map(dao -> {
                    try {
                        dao.restore(keys);
                        return Result.ok();
                    } catch (Exception err) {
                        return Result.err(err);
                    }
                }).orElse(Result.ok());
    }

    /**
     * 新增/修改一个
     *
     * @param info
     * @return
     */
    @PostMapping("/domain")
    public Object create(@RequestBody Registration info) throws Exception {

        container.add(info);
        return Result.ok();

    }

    /**
     * 移除一个domain
     *
     * @param domain
     * @return
     */
    @DeleteMapping("/domain")
    public Object remove(@RequestParam String domain) throws Exception {
        container.remove(domain);
        return Result.ok();
    }


    /**
     * 获取 agg 实例
     *
     * @param domain
     * @return
     */
    @GetMapping("/domain")
    public Object get(@RequestParam String domain, @RequestParam String row) {
//        return Result.ok(container.get(domain, row));

        return null;

    }

}
