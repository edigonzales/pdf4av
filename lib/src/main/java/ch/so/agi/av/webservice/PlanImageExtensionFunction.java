package ch.so.agi.av.webservice;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.StringValue;

/**
 * Gemeinsame Saxon-Extensionfunktion fuer georeferenzierte Planbilder mit Overlay.
 */
abstract class PlanImageExtensionFunction extends ExtensionFunctionDefinition {
    private final StructuredQName functionName;
    private final double mapWidthMm;
    private final double mapHeightMm;

    PlanImageExtensionFunction(String localName, double mapWidthMm, double mapHeightMm) {
        this.functionName = new StructuredQName("av", "http://pdf4av.so.ch/av", localName);
        this.mapWidthMm = mapWidthMm;
        this.mapHeightMm = mapHeightMm;
    }

    @Override
    public StructuredQName getFunctionQName() {
        return functionName;
    }

    @Override
    public SequenceType[] getArgumentTypes() {
        return new SequenceType[]{
                SequenceType.OPTIONAL_NODE,
                SequenceType.OPTIONAL_NODE,
                SequenceType.SINGLE_STRING
        };
    }

    @Override
    public SequenceType getResultType(SequenceType[] suppliedArgumentTypes) {
        return SequenceType.SINGLE_STRING;
    }

    @Override
    public ExtensionFunctionCall makeCallExpression() {
        return new ExtensionFunctionCall() {
            @Override
            public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {
                NodeInfo planNode = headNode(arguments[0]);
                NodeInfo limitNode = headNode(arguments[1]);
                String locale = arguments[2] == null ? "" : arguments[2].head().getStringValue();
                return new StringValue(PlanImageRenderer.renderPlanImage(planNode, limitNode, locale, mapWidthMm, mapHeightMm));
            }
        };
    }

    private static NodeInfo headNode(Sequence sequence) throws XPathException {
        if (sequence == null) {
            return null;
        }

        Item item = sequence.head();
        return item instanceof NodeInfo nodeInfo ? nodeInfo : null;
    }
}
