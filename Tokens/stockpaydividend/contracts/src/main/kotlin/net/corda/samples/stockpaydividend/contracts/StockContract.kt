package net.corda.samples.stockpaydividend.contracts

import com.r3.corda.lib.tokens.contracts.EvolvableTokenContract
import net.corda.core.contracts.CommandData
import net.corda.core.contracts.Contract
import net.corda.core.contracts.Requirements
import net.corda.core.contracts.requireThat
import net.corda.core.transactions.LedgerTransaction
import net.corda.samples.stockpaydividend.states.StockState
import java.math.BigDecimal

// ************
// * Contract *
// ************
class StockContract : EvolvableTokenContract(),Contract {

    @Throws(IllegalArgumentException::class)
    override fun verify(tx: LedgerTransaction) {
        val outputState: StockState = tx.getOutput(0) as StockState
        if (!tx.getCommand<CommandData>(0).signers.contains(outputState.issuer.owningKey)) throw IllegalArgumentException("Company Signature Required")
    }


    override fun additionalCreateChecks(tx: LedgerTransaction) { // Number of outputs is guaranteed as 1
        val createdStockState: StockState = tx.outputsOfType(StockState::class.java)[0]
        requireThat{
            //Validations when creating a new stock
            "Stock symbol must not be empty".using(!createdStockState.symbol.isEmpty())
            "Stock name must not be empty".using(!createdStockState.name.isEmpty())
            "Stock dividend must start with zero".using(!createdStockState.dividend.equals(BigDecimal.ZERO))
        }
    }

    override fun additionalUpdateChecks(tx: LedgerTransaction) { // Number of inputs and outputs are guaranteed as 1
        val input: StockState = tx.inputsOfType(StockState::class.java)[0]
        val output: StockState = tx.outputsOfType(StockState::class.java)[0]
        requireThat{
            //Validations when a stock is updated, ie. AnnounceDividend (UpdateEvolvableToken)
            "Stock Symbol must not be changed.".using(input.symbol == output.symbol)
            "Stock Currency must not be changed.".using(input.currency == output.currency)
            "Stock Name must not be changed.".using(input.name == output.name)
            "Stock Company must not be changed.".using(input.issuer == output.issuer)
        }
    }

}