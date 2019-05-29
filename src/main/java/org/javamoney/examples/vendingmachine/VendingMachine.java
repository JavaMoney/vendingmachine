package org.javamoney.examples.vendingmachine;

import java.util.EnumMap;
import java.util.Map;

import javax.money.MonetaryException;

public class VendingMachine {

	public static class NotPaidException extends MonetaryException {
		private static final long serialVersionUID = -7029912938545985254L;

		public NotPaidException() {
			super("");
		}
	}

	public static class NotEnoughChangeException extends Exception {
		private static final long serialVersionUID = -8739694577318278489L;
	}

	public static class InsufficentPaymentException extends Exception {
		private static final long serialVersionUID = -8061913345859455513L;
	}

	static enum Cash {
		FIVE_HUNDRED_EURO(50000), TWO_HUNDRED_EURO(20000), ONE_HUNDRED_EURO(10000), FIFTY_EURO(5000), TWENTY_EURO(2000),
		TEN_EURO(1000), FIVE_EURO(500), TWO_EURO(200), ONE_EURO(100), FIFTY_CENT(50), TWENTY_CENT(20), TEN_CENT(10),
		FIVE_CENT(5), TWO_CENT(2), ONE_CENT(1);

		private final int value;

		Cash(int value) {
			this.value = value;
		}

		public int getValue() {
			return value;
		}
	}

	static enum Ticket {
		MINI_TICKET(150), INNER_ZONES(540), ALL_ZONES(1030);

		private final int price;

		Ticket(int price) {
			this.price = price;
		}

		public int getPrice() {
			return price;
		}
	}

	private Map<Cash, Integer> cashBox = new EnumMap<>(Cash.class);
	private Map<Ticket, Integer> selectedTickets = new EnumMap<>(Ticket.class);

	public VendingMachine() {
		for (Cash type : Cash.values()) {
			cashBox.put(type, 0);
		}
	}

	private int paidSum = 0;
	private boolean hasBeenPaid = false;

	/**
	 * Refills the vending machine by adding the given amount of money to the its
	 * cashbox.
	 * 
	 * @param cash   Type of coin/note
	 * @param amount Amount of coins/notes
	 */
	public void recharge(Cash cash, int amount) {
		if (amount <= 0) {
			throw new IllegalArgumentException("Amount for rechage must be greater than zero");
		}
		Integer currentAmount = cashBox.get(cash);
		cashBox.put(cash, currentAmount + amount);
	}

	/**
	 * Returns how many coins/notes are available for a given type.
	 * 
	 * @param coinOrNote Type of coin/note
	 * @return
	 */
	public int getAvailableAmount(Cash coinOrNote) {
		return cashBox.get(coinOrNote);
	}

	/**
	 * User inserts a coin/note to pay his ticket.
	 * 
	 * @param coinOrNote Type of coin/note
	 */
	public void insertMoney(Cash coinOrNote) {
		if (hasBeenPaid) {
			throw new IllegalStateException("Can't add a ticket after a transaction has been finished");
		}
		addMoneyToCashbox(coinOrNote);
	}

	private void addMoneyToCashbox(Cash coinOrNote) {
		Integer currentAmount = cashBox.get(coinOrNote);
		cashBox.put(coinOrNote, currentAmount + 1);
		paidSum += coinOrNote.getValue();
	}

	/**
	 * User adds a ticket to its shopping basket
	 * 
	 * @param tariff Ticket type
	 */
	public void selectTicket(Ticket tariff) {
		if (hasBeenPaid) {
			throw new IllegalStateException("Can't add a ticket after a transaction has been finished");
		}
		addTicketToSelection(tariff);
	}

	private void addTicketToSelection(Ticket tariff) {
		if (selectedTickets.containsKey(tariff)) {
			Integer amount = selectedTickets.get(tariff);
			selectedTickets.put(tariff, amount + 1);
		} else {
			selectedTickets.put(tariff, 1);
		}
	}

	public int getCurrentPrice() {
		return sumSelectedTicketPrices();
	}

	public int getPaidSum() {
		return paidSum;
	}

	/**
	 * User wants to finish the transaction and get its change.
	 * 
	 * Prerequisite: User has selected its desired tickets and should have already
	 * inserted money.
	 * 
	 * @return The change
	 */
	public Map<Cash, Integer> buy() throws NotEnoughChangeException, InsufficentPaymentException {
		Map<Cash, Integer> change = calcChangeForTicket();
		hasBeenPaid = true;
		paidSum = 0;
		return change;
	}

	private Map<Cash, Integer> calcChangeForTicket() throws NotEnoughChangeException, InsufficentPaymentException {
		int totalPrice = sumSelectedTicketPrices();
		if (paidSum < totalPrice) {
			throw new InsufficentPaymentException();
		}
		int remainingSum = paidSum - totalPrice;
		Map<Cash, Integer> change = calcChangeOrRefund(remainingSum);
		removeFromCashbox(change);
		return change;
	}

	private int sumSelectedTicketPrices() {
		int totalPrice = 0;
		for (Ticket ticket : selectedTickets.keySet()) {
			totalPrice += selectedTickets.get(ticket) * ticket.price;
		}
		return totalPrice;
	}

	private Map<Cash, Integer> calcChangeOrRefund(int remainingSum) throws NotEnoughChangeException {
		Map<Cash, Integer> change = new EnumMap<>(Cash.class);
		for (Cash type : Cash.values()) {
			int amount = calcMaxAmountOfMoneySmallerThan(type, remainingSum);
			if (amount > 0) {
				change.put(type, amount);
				remainingSum -= amount * type.getValue();
			}
		}
		if (remainingSum != 0) {
			throw new NotEnoughChangeException();
		}
		return change;
	}

	private void removeFromCashbox(Map<Cash, Integer> notesAndCoins) {
		for (Cash noteOrCoin : notesAndCoins.keySet()) {
			int current = cashBox.get(noteOrCoin);
			int diff = notesAndCoins.get(noteOrCoin);
			cashBox.put(noteOrCoin, current - diff);
		}
	}

	private int calcMaxAmountOfMoneySmallerThan(Cash type, int sum) {
		return Math.min(sum / type.getValue(), cashBox.get(type));
	}

	/**
	 * User wants to take the tickets from the machine.
	 * 
	 * @throws NotPaidException
	 * @return The selected tickets or an exception if no payment was done
	 */
	public Map<Ticket, Integer> takeTickets() {
		if (!hasBeenPaid) {
			throw new NotPaidException();
		}
		hasBeenPaid = false;
		Map<Ticket, Integer> order = selectedTickets;
		selectedTickets = new EnumMap<>(Ticket.class);
		return order;
	}

	/**
	 * Cancels the transaction and refunds the amount of money the user has
	 * inserted.
	 * 
	 * @throws IllegalStateException
	 * @return Refund
	 */
	public Map<Cash, Integer> cancel() {
		Map<Cash, Integer> refund = calcRefund();
		paidSum = 0;
		selectedTickets.clear();
		removeFromCashbox(refund);
		return refund;
	}

	private Map<Cash, Integer> calcRefund() {
		if (hasBeenPaid) {
			throw new IllegalStateException(
					"Cancel is not possible after a ticket has been bought (and not been fetched)");
		}
		try {
			Map<Cash, Integer> refund = calcChangeOrRefund(paidSum);
			return refund;
		} catch (NotEnoughChangeException ex) {
			throw new IllegalStateException("Vending machine didn't had enough money to refund user", ex);
		}
	}
}
