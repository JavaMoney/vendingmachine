package org.javamoney.examples.vendingmachine;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.util.Map;

import org.javamoney.examples.vendingmachine.VendingMachine;
import org.javamoney.examples.vendingmachine.VendingMachine.Cash;
import org.javamoney.examples.vendingmachine.VendingMachine.InsufficentPaymentException;
import org.javamoney.examples.vendingmachine.VendingMachine.NotEnoughChangeException;
import org.javamoney.examples.vendingmachine.VendingMachine.Ticket;
import org.junit.Test;

public class VendingMachineHamcrestTest {

	private VendingMachine createWellFilledMachine() {
		VendingMachine machine = new VendingMachine();
		for (Cash type : Cash.values()) {
			machine.recharge(type, 10);
		}
		return machine;
	}

	@Test
	public void init() {
		VendingMachine machine = new VendingMachine();

		assertThat(machine.getAvailableAmount(Cash.FIFTY_EURO), is(0));
	}

	@Test
	public void recharge() {
		VendingMachine machine = new VendingMachine();

		machine.recharge(Cash.FIVE_HUNDRED_EURO, 5);
		machine.recharge(Cash.FIVE_HUNDRED_EURO, 2);

		assertThat(machine.getAvailableAmount(Cash.FIVE_HUNDRED_EURO), is(7));
	}

	@Test
	public void buyATicket() throws NotEnoughChangeException, InsufficentPaymentException {
		VendingMachine machine = createWellFilledMachine();

		machine.selectTicket(Ticket.INNER_ZONES);
		machine.insertMoney(Cash.TEN_EURO);
		Map<Cash, Integer> change = machine.buy();
		Map<Ticket, Integer> tickets = machine.takeTickets();

		assertThat(change.keySet(), hasSize(3));
		assertThat(change, hasEntry(Cash.TWO_EURO, 2));
		assertThat(change, hasEntry(Cash.FIFTY_CENT, 1));
		assertThat(change, hasEntry(Cash.TEN_CENT, 1));

		assertThat(tickets.keySet(), hasSize(1));
		assertThat(tickets, hasEntry(Ticket.INNER_ZONES, 1));
	}
}
