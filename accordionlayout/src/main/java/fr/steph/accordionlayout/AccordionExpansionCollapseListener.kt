package fr.steph.accordionlayout

/**
 * Created by riyagayasen on 24/10/16.
 * Updated by Stéphane Bernardelli on 12/27/22
 */

/***
 * This interface acts as a listener for the expansion and collapse of the accordion
 */
interface AccordionExpansionCollapseListener {
    fun onExpanded(view: AccordionLayout)
    fun onCollapsed(view: AccordionLayout)
}