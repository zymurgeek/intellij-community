// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.intellij.stats.completion

import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupEvent
import com.intellij.codeInsight.lookup.LookupListener
import com.intellij.codeInsight.lookup.impl.LookupImpl
import com.intellij.stats.personalization.UserFactorDescriptions
import com.intellij.stats.personalization.UserFactorStorage

/**
 * @author Vitaliy.Bibaev
 */
class LookupCompletedTracker : LookupListener {
    override fun lookupCanceled(event: LookupEvent) {
        val lookup = event.lookup as? LookupImpl ?: return
        val element = lookup.currentItem
        if (element != null && isSelectedByTyping(lookup, element)) {
            processTypedSelect(lookup)
        } else {
            UserFactorStorage.applyOnBoth(lookup.project, UserFactorDescriptions.COMPLETION_FINISH_TYPE) { updater ->
                updater.fireLookupCancelled()
            }
        }
    }

    override fun itemSelected(event: LookupEvent) {
        val lookup = event.lookup as? LookupImpl ?: return
        val element = event.item ?: return
        processExplicitSelect(lookup, element)
    }

    private fun isSelectedByTyping(lookup: LookupImpl, element: LookupElement): Boolean =
            element.lookupString == lookup.itemPattern(element)


    private fun processExplicitSelect(lookup: LookupImpl, element: LookupElement) {
        UserFactorStorage.applyOnBoth(lookup.project, UserFactorDescriptions.COMPLETION_FINISH_TYPE) { updater ->
            updater.fireExplicitCompletionPerformed()
        }

        val prefixLength = lookup.getPrefixLength(element)
        UserFactorStorage.applyOnBoth(lookup.project, UserFactorDescriptions.PREFIX_LENGTH_ON_COMPLETION) { updater ->
            updater.fireCompletionPerformed(prefixLength)
        }

        val itemPosition = lookup.selectedIndex
        if (itemPosition != -1) {
            UserFactorStorage.applyOnBoth(lookup.project, UserFactorDescriptions.SELECTED_ITEM_POSITION) { updater ->
                updater.fireCompletionPerformed(itemPosition)
            }
        }

        if (prefixLength > 1) {
            val pattern = lookup.itemPattern(element)
            val isMnemonicsUsed = !element.lookupString.startsWith(pattern)
            UserFactorStorage.applyOnBoth(lookup.project, UserFactorDescriptions.MNEMONICS_USAGE) { updater ->
                updater.fireCompletionFinished(isMnemonicsUsed)
            }
        }
    }

    private fun processTypedSelect(lookup: LookupImpl) {
        UserFactorStorage.applyOnBoth(lookup.project, UserFactorDescriptions.COMPLETION_FINISH_TYPE) { updater ->
            updater.fireTypedSelectPerformed()
        }
    }
}
