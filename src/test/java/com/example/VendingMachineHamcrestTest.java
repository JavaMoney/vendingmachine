package com.example;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.util.Map;

import org.junit.Test;

import com.example.VendingMachine.InsufficentPaymentException;
import com.example.VendingMachine.Money;
import com.example.VendingMachine.NotEnoughChangeException;
import com.example.VendingMachine.Ticket;

public class VendingMachineHamcrestTest {

	private VendingMachine createWellFilledMachine() {
		VendingMachine machine = new VendingMachine();
		for (Money type : Money.values()) {
			machine.recharge(type, 10);
		}
		return machine;
	}

	@Test
	public void init() {
		VendingMachine machine = new VendingMachine();

		assertThat(machine.getAvailableAmount(Money.FIFTY_EURO), is(0));
	}

	@Test
	public void recharge() {
		VendingMachine machine = new VendingMachine();

		machine.recharge(Money.FIVE_HUNDRED_EURO, 5);
		machine.recharge(Money.FIVE_HUNDRED_EURO, 2);

		assertThat(machine.getAvailableAmount(Money.FIVE_HUNDRED_EURO), is(7));
	}

	@Test
	public void buyATicket() throws NotEnoughChangeException, InsufficentPaymentException {
		VendingMachine machine = createWellFilledMachine();

		machine.selectTicket(Ticket.INNER_ZONES);
		machine.insertMoney(Money.TEN_EURO);
		Map<Money, Integer> change = machine.buy();
		Map<Ticket, Integer> tickets = machine.takeTickets();

		assertThat(change.keySet(), hasSize(3));
		assertThat(change, hasEntry(Money.TWO_EURO, 2));
		assertThat(change, hasEntry(Money.FIFTY_CENT, 1));
		assertThat(change, hasEntry(Money.TEN_CENT, 1));

		assertThat(tickets.keySet(), hasSize(1));
		assertThat(tickets, hasEntry(Ticket.INNER_ZONES, 1));
	}
}
