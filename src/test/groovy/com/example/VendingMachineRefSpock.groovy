package com.example

import spock.lang.*
import org.junit.Test

import com.example.VendingMachine.Money
import com.example.VendingMachine.Ticket

class SpockDataTableTest extends Specification {
	
	private def createWellFilledMachine() {
		def machine = new VendingMachine()
		Money.values().each { type ->  machine.recharge(type, 10) }
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
	        [Ticket.MINI_TICKET] | [Money.TEN_EURO]  | [(Money.FIVE_EURO)   : 1, 
														(Money.TWO_EURO)    : 1,
														(Money.ONE_EURO)    : 1,
														(Money.FIFTY_CENT)  : 1]
			[Ticket.INNER_ZONES] | [Money.FIFTY_EURO]| [(Money.TWENTY_EURO) : 2,
														(Money.TWO_EURO)    : 2,
														(Money.FIFTY_CENT)  : 1,
														(Money.TEN_CENT)    : 1]
	}
}