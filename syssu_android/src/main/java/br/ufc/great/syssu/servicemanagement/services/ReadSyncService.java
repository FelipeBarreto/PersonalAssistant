package br.ufc.great.syssu.servicemanagement.services;

import java.util.List;

import br.ufc.great.syssu.base3.Pattern;
import br.ufc.great.syssu.base3.Scope;
import br.ufc.great.syssu.base3.Tuple;
import br.ufc.great.syssu.base3.TupleSpaceException;
import br.ufc.great.syssu.base3.TupleSpaceSecurityException;
import br.ufc.great.syssu.base3.interfaces.IDomain;

public class ReadSyncService extends AbstractQueryService {

    @Override
    public String getName() {
        return "readsync";
    }

	@Override
	protected List<Tuple> query(IDomain domain, Pattern pattern, String restriction, String key, long timeout, Scope scope)
		throws TupleSpaceException, TupleSpaceSecurityException {
		return domain.readSync(pattern, restriction, key, timeout);	}
}

