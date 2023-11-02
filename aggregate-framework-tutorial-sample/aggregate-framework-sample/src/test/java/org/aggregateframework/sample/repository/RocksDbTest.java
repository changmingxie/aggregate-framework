package org.aggregateframework.sample.repository;

import org.aggregateframework.sample.AbstractTestCase;
import org.aggregateframework.transaction.Transaction;
import org.aggregateframework.transaction.serializer.TransactionSerializer;
import org.aggregateframework.transaction.serializer.kryo.RegisterableKryoTransactionSerializer;
import org.aggregateframework.xid.TransactionXid;
import org.junit.Test;
import org.rocksdb.Options;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.rocksdb.RocksIterator;

public class RocksDbTest extends AbstractTestCase {

    private TransactionSerializer serializer = new RegisterableKryoTransactionSerializer();


    @Test
    public void given_path_when_open_rocksdb_and_put_transaction_and_iterator_the_value_then_can_found() throws RocksDBException {

        try (Options options = new Options().setCreateIfMissing(true)) {

            try (RocksDB db = RocksDB.open(options, "/tmp/recoverystore-test/")) {

                Transaction transaction = new Transaction(TransactionXid.withUniqueIdentity(null));

                String prefix = "AGG:ROCKSDB:";
                db.put((prefix + transaction.getXid().toString()).getBytes(), serializer.serialize(transaction));

                System.out.println("all begin");

                try (final RocksIterator iterator = db.newIterator()) {

                    for (iterator.seekToFirst(); iterator.isValid(); iterator.next()) {
                        String key = new String(iterator.key());
                        System.out.println(String.format("%s", new String(iterator.key())));
                    }
                }

                System.out.println("all end");

                System.out.println("prefix begin");
                try (final RocksIterator iterator = db.newIterator()) {

                    for (iterator.seek(prefix.getBytes()); iterator.isValid(); iterator
                            .next()) {
                        String key = new String(iterator.key());

                        if (!key.startsWith(prefix))
                            break;

                        System.out.println(String.format("%s", new String(iterator.key())));
                    }
                }

                System.out.println("prefix end");
            }
        }
    }
}
