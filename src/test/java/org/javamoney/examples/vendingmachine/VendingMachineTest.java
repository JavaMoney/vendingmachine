package org.javamoney.examples.vendingmachine;

import java.util.EnumMap;
import java.util.Map;

import org.javamoney.examples.vendingmachine.VendingMachine;
import org.javamoney.examples.vendingmachine.VendingMachine.Cash;
import org.javamoney.examples.vendingmachine.VendingMachine.InsufficentPaymentException;
import org.javamoney.examples.vendingmachine.VendingMachine.NotEnoughChangeException;
import org.javamoney.examples.vendingmachine.VendingMachine.NotPaidException;
import org.junit.*;

import static org.javamoney.examples.vendingmachine.VendingMachine.*;
import static org.junit.Assert.*;

public class VendingMachineTest {

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
		assertEquals(0, machine.getAvailableAmount(Cash.FIFTY_EURO));
	}

	@Test
	public void recharge() {
		VendingMachine machine = new VendingMachine();
		machine.recharge(Cash.FIVE_HUNDRED_EURO, 5);
		machine.recharge(Cash.FIVE_HUNDRED_EURO, 2);
		assertEquals(7, machine.getAvailableAmount(Cash.FIVE_HUNDRED_EURO));
	}

	@Test
	public void buyATicket() throws NotEnoughChangeException, InsufficentPaymentException {
		VendingMachine machine = createWellFilledMachine();
		machine.selectTicket(Ticket.INNER_ZONES);
		machine.insertMoney(Cash.TEN_EURO);
		Map<Cash, Integer> change = machine.buy();
		EnumMap<Cash, Integer> expMoney = new EnumMap<Cash, Integer>(Cash.class);
		expMoney.put(Cash.TWO_EURO, 2);
		expMoney.put(Cash.FIFTY_CENT, 1);
		expMoney.put(Cash.TEN_CENT, 1);
		assertEquals(expMoney, change);
		EnumMap<Ticket, Integer> expTickets = new EnumMap<Ticket, Integer>(Ticket.class);
		expTickets.put(Ticket.INNER_ZONES, 1);
		assertEquals(expTickets, machine.takeTickets());

		assertEquals(11, machine.getAvailableAmount(Cash.TEN_EURO));
		assertEquals(8, machine.getAvailableAmount(Cash.TWO_EURO));
		assertEquals(9, machine.getAvailableAmount(Cash.FIFTY_CENT));
		assertEquals(9, machine.getAvailableAmount(Cash.TEN_CENT));
	}

	@Test
	public void twoTransactions() throws NotEnoughChangeException, InsufficentPaymentException {
		VendingMachine machine = createWellFilledMachine();
		machine.selectTicket(Ticket.INNER_ZONES);
		machine.insertMoney(Cash.TEN_EURO);
		machine.buy();
		machine.takeTickets();
		machine.selectTicket(Ticket.ALL_ZONES);
		machine.insertMoney(Cash.TEN_EURO);
		machine.insertMoney(Cash.TEN_CENT);
		machine.insertMoney(Cash.TEN_CENT);
		machine.insertMoney(Cash.TEN_CENT);
		Map<Cash, Integer> change = machine.buy();
		EnumMap<Cash, Integer> expMoney = new EnumMap<Cash, Integer>(Cash.class);
		assertEquals(expMoney, change);
		EnumMap<Ticket, Integer> expTickets = new EnumMap<Ticket, Integer>(Ticket.class);
		expTickets.put(Ticket.ALL_ZONES, 1);
		assertEquals(expTickets, machine.takeTickets());
	}

	@Test(expected = NotPaidException.class)
	public void notPaid() throws NotEnoughChangeException, InsufficentPaymentException {
		VendingMachine machine = createWellFilledMachine();
		machine.selectTicket(Ticket.INNER_ZONES);
		machine.insertMoney(Cash.TEN_EURO);
		machine.takeTickets();
	}

	@Test
	public void transactionCanceled() throws NotEnoughChangeException, InsufficentPaymentException {
		VendingMachine machine = createWellFilledMachine();
		machine.selectTicket(Ticket.INNER_ZONES);
		machine.insertMoney(Cash.TEN_EURO);
		Map<Cash, Integer> refund = machine.cancel();
		EnumMap<Cash, Integer> expMoney = new EnumMap<Cash, Integer>(Cash.class);
		expMoney.put(Cash.TEN_EURO, 1);
		assertEquals(expMoney, refund);
		assertEquals(0, machine.getCurrentPrice());
		assertEquals(0, machine.getPaidSum());
	}

	@Test(expected = NotEnoughChangeException.class)
	public void notEnoughChange() throws NotEnoughChangeException, InsufficentPaymentException {
		VendingMachine machine = new VendingMachine();
		machine.selectTicket(Ticket.INNER_ZONES);
		machine.insertMoney(Cash.TEN_EURO);
		machine.buy();
	}

	@Test
	public void notEnoughChangeDoesNotAlterAvailableMoney()
			throws NotEnoughChangeException, InsufficentPaymentException {
		VendingMachine machine = new VendingMachine();
		machine.recharge(Cash.TWENTY_CENT, 1);
		machine.recharge(Cash.TEN_CENT, 2);
		machine.selectTicket(Ticket.MINI_TICKET);
		machine.insertMoney(Cash.TWO_EURO);
		try {
			machine.buy();
			fail("Exception expected");
		} catch (NotEnoughChangeException ignored) {
			// The purpose of this test it to check the post exception state
		}
		assertEquals(1, machine.getAvailableAmount(Cash.TWO_EURO));
		assertEquals(1, machine.getAvailableAmount(Cash.TWENTY_CENT));
		assertEquals(2, machine.getAvailableAmount(Cash.TEN_CENT));
	}

	@Test(expected = IllegalStateException.class)
	public void finishedTransactionCanceled() throws NotEnoughChangeException, InsufficentPaymentException {
		VendingMachine machine = createWellFilledMachine();
		machine.selectTicket(Ticket.INNER_ZONES);
		machine.insertMoney(Cash.TEN_EURO);
		machine.buy();
		machine.cancel();
	}

	@Test(expected = IllegalStateException.class)
	public void addedTicketAfterTransactionFinished() throws NotEnoughChangeException, InsufficentPaymentException {
		VendingMachine machine = createWellFilledMachine();
		machine.selectTicket(Ticket.INNER_ZONES);
		machine.insertMoney(Cash.TEN_EURO);
		machine.buy();
		machine.selectTicket(Ticket.ALL_ZONES);
		machine.takeTickets();
	}

	@Test(expected = IllegalStateException.class)
	public void insertedMoneyAfterTransactionFinished() throws NotEnoughChangeException, InsufficentPaymentException {
		VendingMachine machine = createWellFilledMachine();
		machine.selectTicket(Ticket.INNER_ZONES);
		machine.insertMoney(Cash.TEN_EURO);
		machine.buy();
		machine.insertMoney(Cash.TEN_EURO);
		machine.takeTickets();
	}

	@Test(expected = NotPaidException.class)
	public void twoTransactionsLastNotPaid() throws NotEnoughChangeException, InsufficentPaymentException {
		VendingMachine machine = createWellFilledMachine();
		machine.selectTicket(Ticket.INNER_ZONES);
		machine.insertMoney(Cash.TEN_EURO);
		machine.buy();
		machine.takeTickets();
		machine.selectTicket(Ticket.INNER_ZONES);
		machine.takeTickets();
	}

	@Test(expected = InsufficentPaymentException.class)
	public void notEnoughMoneyForTicket() throws NotEnoughChangeException, InsufficentPaymentException {
		VendingMachine machine = createWellFilledMachine();
		machine.selectTicket(Ticket.INNER_ZONES);
		machine.insertMoney(Cash.ONE_EURO);
		machine.buy();
	}
}
