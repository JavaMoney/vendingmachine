package org.javamoney.examples.vendingmachine

import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.*

import org.junit.Test

import org.javamoney.examples.vendingmachine.VendingMachine.Cash
import org.javamoney.examples.vendingmachine.VendingMachine.Ticket

class VendingMachineHamcrestWithGroovyTest {

	private def createWellFilledMachine() {
		def machine = new VendingMachine()
		Cash.values().each { type ->  machine.recharge(type, 10) }
		return machine
	}

	@Test
	void init() {
		def machine = new VendingMachine()

		assertThat machine.getAvailableAmount(Cash.FIFTY_EURO), is(0)
	}

	@Test
	void recharge() {
		def machine = new VendingMachine()

		machine.recharge Cash.FIVE_HUNDRED_EURO, 5
		machine.recharge Cash.FIVE_HUNDRED_EURO, 2

		assertThat machine.getAvailableAmount(Cash.FIVE_HUNDRED_EURO), is(7)
	}

	@Test
	void buyATicket() {
		def machine = createWellFilledMachine()

		machine.selectTicket Ticket.INNER_ZONES
		machine.insertMoney Cash.TEN_EURO
		def change = machine.buy()
		def tickets = machine.takeTickets()

		assertThat change.keySet(), hasSize(3)
		assertThat change, hasEntry(Cash.TWO_EURO, 2)
		assertThat change, hasEntry(Cash.FIFTY_CENT, 1)
		assertThat change, hasEntry(Cash.TEN_CENT, 1)

		assertThat tickets.keySet(), hasSize(1)
		assertThat tickets, hasEntry(Ticket.INNER_ZONES, 1)
	}
}
