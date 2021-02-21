/*
 * Copyright (c) 2020  RS485
 *
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public
 * License 1.0.1, or MMPL. Please check the contents of the license located in
 * https://github.com/RS485/LogisticsPipes/blob/dev/LICENSE.md
 *
 * This file can instead be distributed under the license terms of the
 * MIT license:
 *
 * Copyright (c) 2020  RS485
 *
 * This MIT license was reworded to only match this file. If you use the regular
 * MIT license in your project, replace this copyright notice (this line and any
 * lines below and NOT the copyright line above) with the lines from the original
 * MIT license located here: http://opensource.org/licenses/MIT
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this file and associated documentation files (the "Source Code"), to deal in
 * the Source Code without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Source Code, and to permit persons to whom the Source Code is furnished
 * to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Source Code, which also can be
 * distributed under the MIT.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package network.rs485.logisticspipes.gui.guidebook

import net.minecraft.nbt.NBTTagCompound
import network.rs485.logisticspipes.guidebook.BookContents
import network.rs485.logisticspipes.util.LPDataInput
import network.rs485.logisticspipes.util.LPDataOutput
import network.rs485.logisticspipes.util.LPFinalSerializable
import network.rs485.logisticspipes.util.cycleMinecraftColorId
import network.rs485.logisticspipes.util.math.Rectangle


class SavedPage(val page: String) : LPFinalSerializable {
    var color: Int? = null
    var progress: Float = 0.0F

    constructor(page: String, color: Int?, progress: Float) : this(page) {
        this.color = color
        this.progress = progress
    }

    private val pageInfo = BookContents.get(page)
    val drawablePage = BookContents.getDrawablePage(page)
    val title: String
        get() = pageInfo.metadata.title

    fun updateScrollPosition(visibleArea: Rectangle) =
        drawablePage.updateScrollPosition(visibleArea, progress)

    fun mouseClicked(mouseX: Int, mouseY: Int, visibleArea: Rectangle, guideActionListener: GuiGuideBook.ActionListener) {
        drawablePage.getVisibleParagraphs(visibleArea)
            .firstOrNull { it.absoluteBody.contains(mouseX, mouseY) }
            ?.mouseClicked(mouseX, mouseY, guideActionListener)
    }

    fun setDrawablesPosition(area: Rectangle) {
        drawablePage.setWidth(area.width)
        drawablePage.setPos(area.x0, area.y0)
    }

    /**
     * Takes in an LPDataOutput buffer and turns a SavedPage object into bytes and puts them inside the buffer.
     * @param output data to send
     */
    override fun write(output: LPDataOutput) = output.writeNBTTagCompound(toTag())

    fun toTag(): NBTTagCompound {
        val nbt = NBTTagCompound()
        nbt.setString("page", page)
        color?.also {
            nbt.setInteger("color", it)
        }
        nbt.setFloat("progress", progress)
        return nbt
    }

    companion object {
        fun fromTag(nbt: NBTTagCompound?): SavedPage {
            return if (nbt != null) SavedPage(
                page = nbt.getString("page"),
                color = if (nbt.hasKey("color")) nbt.getInteger("color") else null,
                progress = nbt.getFloat("progress")
            ) else SavedPage(BookContents.DEBUG_FILE)
        }

        /**
         * Takes in an LPDataInput buffer and turns the buffered bytes object into a SavedPage object.
         * @param input the received data
         * @return SavedPage object created from the buffered data
         */
        @JvmStatic
        fun fromBytes(input: LPDataInput): SavedPage {
            return fromTag(input.readNBTTagCompound())
        }
    }

    fun pageEquals(other: SavedPage): Boolean = this.page == other.page

    fun cycleColor(inverted: Boolean = false) = cycleMinecraftColorId((color ?: 0), inverted).also { color = it }

}
