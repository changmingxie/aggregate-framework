package org.aggregateframework.sample.complexmodel.command.domain.entity;

import org.aggregateframework.entity.AbstractSimpleAggregateRoot;
import org.aggregateframework.entity.DaoAwareQuery;
import org.aggregateframework.sample.complexmodel.command.domain.event.OrderCreatedEvent;
import org.aggregateframework.sample.complexmodel.command.domain.event.OrderUpdatedEvent;
import org.aggregateframework.sample.complexmodel.command.domain.event.SeatAvailabilityRemovedEvent;

import java.util.ArrayList;
import java.util.List;

public class BookingOrder extends AbstractSimpleAggregateRoot<UserShardingId> {

    private static final long serialVersionUID = -1431035454011931259L;
    private String content;

    @DaoAwareQuery(mappedBy = "bookingOrder", select = "findByOrderId")
    private List<SeatAvailability> seatAvailabilities = new ArrayList<SeatAvailability>();

    private BookingPayment bookingPayment;

    private boolean recovered;
    private UserShardingId id;

    public BookingOrder() {
        apply(new OrderCreatedEvent(this));
    }

    @Override
    public UserShardingId getId() {
        return id;
    }

    @Override
    public void setId(UserShardingId userShardingId) {
        this.id = userShardingId;
    }

    public String getContent() {
        return content;
    }

    public void updateContent(String content) {
        this.content = content;
        this.apply(new OrderUpdatedEvent(this.getId(), content));
    }

    public void removeSeatAvailabilities() {
        seatAvailabilities.clear();
        this.apply(new SeatAvailabilityRemovedEvent(this));
    }

    public BookingPayment getBookingPayment() {
        return bookingPayment;
    }

    public void updatePayment(BookingPayment bookingPayment) {
        this.bookingPayment = bookingPayment;
    }


    public List<SeatAvailability> getSeatAvailabilities() {
        return seatAvailabilities;
    }

    public void addSeatAvailability(SeatAvailability seatAvailability) {

        seatAvailability.setBookingOrder(this);
        seatAvailabilities.add(seatAvailability);
    }

    public void removeSeatAvailability(SeatAvailability seatAvailability) {
        seatAvailabilities.remove(seatAvailability);
    }


    public void recovered() {
        recovered = true;
    }

    public boolean isRecovered() {
        return recovered;
    }
}
