package com.example

import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.*

import org.junit.Test

import com.example.VendingMachine.Money
import com.example.VendingMachine.Ticket

class VendingMachineHamcrestWithGroovyTest {

	private def createWellFilledMachine() {
		def machine = new VendingMachine()
		Money.values().each { type ->  machine.recharge(type, 10) }
		return machine
	}

	@Test
	void init() {
		def machine = new VendingMachine()

		assertThat machine.getAvailableAmount(Money.FIFTY_EURO), is(0)
	}

	@Test
	void recharge() {
		def machine = new VendingMachine()

		machine.recharge Money.FIVE_HUNDRED_EURO, 5
		machine.recharge Money.FIVE_HUNDRED_EURO, 2

		assertThat machine.getAvailableAmount(Money.FIVE_HUNDRED_EURO), is(7)
	}

	@Test
	void buyATicket() {
		def machine = createWellFilledMachine()

		machine.selectTicket Ticket.INNER_ZONES
		machine.insertMoney Money.TEN_EURO
		def change = machine.buy()
		def tickets = machine.takeTickets()

		assertThat change.keySet(), hasSize(3)
		assertThat change, hasEntry(Money.TWO_EURO, 2)
		assertThat change, hasEntry(Money.FIFTY_CENT, 1)
		assertThat change, hasEntry(Money.TEN_CENT, 1)

		assertThat tickets.keySet(), hasSize(1)
		assertThat tickets, hasEntry(Ticket.INNER_ZONES, 1)
	}
}
