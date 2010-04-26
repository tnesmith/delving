/*
 * Copyright 2007 EDL FOUNDATION
 *
 *  Licensed under the EUPL, Version 1.0 or? as soon they
 *  will be approved by the European Commission - subsequent
 *  versions of the EUPL (the "Licence");
 *  you may not use this work except in compliance with the
 *  Licence.
 *  You may obtain a copy of the Licence at:
 *
 *  http://ec.europa.eu/idabc/eupl
 *
 *  Unless required by applicable law or agreed to in
 *  writing, software distributed under the Licence is
 *  distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *  express or implied.
 *  See the Licence for the specific language governing
 *  permissions and limitations under the Licence.
 */

package eu.europeana.sip.gui;

import eu.europeana.sip.model.AnalysisTree;

import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

/**
 * Render the cells of the analysis tree
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class AnalysisTreeCellRenderer extends DefaultTreeCellRenderer {
    private Font normalFont, thickFont;

    @Override
    public Component getTreeCellRendererComponent(JTree jTree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        JLabel label = (JLabel) super.getTreeCellRendererComponent(jTree, value, selected, expanded, leaf, row, hasFocus);
        AnalysisTree.Node node = (AnalysisTree.Node) value;
        label.setFont(node.getStatistics() != null ? getThickFont() : getNormalFont());
        if (node.isRecordRoot()) {
            label.setForeground(Color.RED);
        }
        return label;
    }

    private Font getNormalFont() {
        if (normalFont == null) {
            normalFont = super.getFont();
        }
        return normalFont;
    }

    private Font getThickFont() {
        if (thickFont == null) {
            thickFont = new Font(getNormalFont().getFontName(), Font.BOLD, getNormalFont().getSize());
        }
        return thickFont;
    }
}
