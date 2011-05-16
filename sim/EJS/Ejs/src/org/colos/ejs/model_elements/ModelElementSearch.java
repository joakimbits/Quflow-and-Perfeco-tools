package org.colos.ejs.model_elements;

import javax.swing.text.JTextComponent;

import org.colos.ejs.osejs.edition.SearchResult;

public class ModelElementSearch extends SearchResult {

  protected ModelElementsCollection mList;
  protected ModelElement mElement;
  
  public ModelElementSearch (ModelElementsCollection list, ModelElement element, String anInformation, String aText, JTextComponent aComponent, int aLineNumber, int aCaretPosition) {
    super (anInformation,aText,aComponent,aLineNumber,aCaretPosition);
    this.mElement = element;
    this.mList = list;
  }

  @Override
  public String toString () {
    return information+"("+lineNumber+"): "+textFound;
  }

  @Override
  public void show () {
   String name = mList.getEJS().getModelEditor().getElementsEditor().selectElement(mElement);
    // Now, make the text visible
    mElement.showEditor(name, mList.getEJS().getMainFrame(), mList);
    containerTextComponent.requestFocusInWindow();
    containerTextComponent.setCaretPosition(caretPosition);
  }

}
