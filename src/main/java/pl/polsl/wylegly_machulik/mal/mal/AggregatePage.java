package pl.polsl.wylegly_machulik.mal.mal;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class AggregatePage {

    private List<Aggregate> aggregates = new LinkedList<>();

    public void append(Aggregate aggregate) {
        aggregates.add(aggregate);
    }

    public Optional<Aggregate> get(int aggregateNumber) {
        if (aggregates.size() > aggregateNumber) {
            return Optional.of(aggregates.get(aggregateNumber));
        } else {
            return Optional.empty();
        }
    }
}