package org.javamoney.examples.vendingmachine

import spock.lang.*
import org.junit.Test

import org.javamoney.examples.vendingmachine.VendingMachine.Cash
import org.javamoney.examples.vendingmachine.VendingMachine.Ticket

class SpockDataTableTest extends Specification {
	
	private def createWellFilledMachine() {
		def machine = new VendingMachine()
		Cash.values().each { type ->  machine.recharge(type, 10) }
		return machine
	}
	
	@Test
	@Unroll('#tickets are ordered, #money is inserted and the machine should return #change')
	def 'buying a ticket'() {
	    expect:
	        def machine = createWellFilledMachine()
			money.each { noteOrCoin -> machine.insertMoney(noteOrCoin)}
			tickets.each { ticket -> machine.selectTicket(ticket)}
			def actChange = machine.buy()
			change == actChange
	    where:
	        tickets 			   | money 	         | change
	        [Ticket.MINI_TICKET] | [Cash.TEN_EURO]  | [(Cash.FIVE_EURO)   : 1, 
														(Cash.TWO_EURO)    : 1,
														(Cash.ONE_EURO)    : 1,
														(Cash.FIFTY_CENT)  : 1]
			[Ticket.INNER_ZONES] | [Cash.FIFTY_EURO]| [(Cash.TWENTY_EURO) : 2,
														(Cash.TWO_EURO)    : 2,
														(Cash.FIFTY_CENT)  : 1,
														(Cash.TEN_CENT)    : 1]
	}
}