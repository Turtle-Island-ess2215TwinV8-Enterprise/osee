/*
 * Created on Mar 17, 2010
 *
 * PLACE_YOUR_DISTRIBUTION_STATEMENT_RIGHT_HERE
 */
package org.eclipse.osee.framework.ui.skynet.test.cases;

import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.util.Lib;
import org.eclipse.osee.framework.jdk.core.util.xml.Jaxp;
import org.eclipse.osee.framework.skynet.core.word.WordUtil;
import org.eclipse.osee.framework.ui.skynet.render.WordTemplateRenderer;
import org.eclipse.osee.framework.ui.skynet.render.artifactElement.WordArtifactElementExtractor;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * Test case for {@link WordArtifactElementExtractor}.
 * @author Jeff C. Phillips
 * 
 */
public class WordArtifactElementExtractorTest {
	private final static String NO_CHANGE = "<w:body></w:body>";
	private final static String CHANGE_IN_BETWEEN_2007 = "<w:body><w:p wsp:rsidR=\"00682312\" wsp:rsidRDefault=\"00B17B94\"><w:hlink w:dest=\"http://none/edit?guid=A+7R6BYOEiR5BY230lgA&amp;branchId=2380\"><w:r wsp:rsidR=\"00A05BEC\"><w:rPr><w:rStyle w:val=\"Hyperlink\"/></w:rPr><w:t>OSEE_EDIT_START</w:t></w:r></w:hlink></w:p><w:p wsp:rsidR=\"002D7BD4\" wsp:rsidRDefault=\"002D7BD4\"><w:r><w:t>Middle change</w:t></w:r></w:p><w:p wsp:rsidR=\"00B17B94\" wsp:rsidRDefault=\"00B17B94\"><w:hlink w:dest=\"http://none/edit?guid=A+7R6BYOEiR5BY230lgA&amp;branchId=2380\"><w:r wsp:rsidR=\"00A05BEC\"><w:rPr><w:rStyle w:val=\"Hyperlink\"/></w:rPr><w:t>OSEE_EDIT_END</w:t></w:r></w:hlink></w:p><w:p wsp:rsidR=\"00B17B94\" wsp:rsidRDefault=\"00B17B94\"/><w:p wsp:rsidR=\"00B17B94\" wsp:rsidRDefault=\"00B17B94\"><w:pPr><w:rPr><w:vanish/></w:rPr></w:pPr><w:r><w:rPr><w:vanish/></w:rPr><w:fldChar w:fldCharType=\"begin\"/></w:r><w:r wsp:rsidR=\"00A05BEC\"><w:rPr><w:vanish/></w:rPr><w:instrText>LISTNUM\"listreset\"\\l1\\s0</w:instrText></w:r><w:r><w:rPr><w:vanish/></w:rPr><w:fldChar w:fldCharType=\"end\"/><wx:t wx:val=\" .\"/></w:r></w:p><w:sectPr wsp:rsidR=\"00B17B94\" wsp:rsidSect=\"007F3E1C\"><w:pgSz w:w=\"12240\" w:h=\"15840\" w:code=\"1\"/><w:pgMar w:top=\"1440\" w:right=\"1440\" w:bottom=\"1440\" w:left=\"1440\" w:header=\"432\" w:footer=\"432\" w:gutter=\"0\"/><w:pgNumType w:start=\"1\"/><w:cols w:space=\"475\"/><w:noEndnote/><w:docGrid w:line-pitch=\"360\"/></w:sectPr></w:body>";
	private final static String START_CHANGE_2007 ="<w:body><w:p wsp:rsidR=\"00B17B94\" wsp:rsidRDefault=\"00B17B94\"><w:hlink w:dest=\"http://none/edit?guid=A+7R6BYOEiR5BY230lgA&amp;branchId=2380\"><w:r wsp:rsidR=\"00BC116F\"><w:rPr><w:rStyle w:val=\"Hyperlink\"/></w:rPr><w:t>OSEE_EDIT_START</w:t></w:r></w:hlink><w:r wsp:rsidR=\"00982F7F\"><w:t>I am change</w:t></w:r></w:p><w:p wsp:rsidR=\"00B17B94\" wsp:rsidRDefault=\"00B17B94\"><w:hlink w:dest=\"http://none/edit?guid=A+7R6BYOEiR5BY230lgA&amp;branchId=2380\"><w:r wsp:rsidR=\"00BC116F\"><w:rPr><w:rStyle w:val=\"Hyperlink\"/></w:rPr><w:t>OSEE_EDIT_END</w:t></w:r></w:hlink></w:p><w:p wsp:rsidR=\"00B17B94\" wsp:rsidRDefault=\"00B17B94\"/><w:p wsp:rsidR=\"00B17B94\" wsp:rsidRDefault=\"00B17B94\"><w:pPr><w:rPr><w:vanish/></w:rPr></w:pPr><w:r><w:rPr><w:vanish/></w:rPr><w:fldChar w:fldCharType=\"begin\"/></w:r><w:r wsp:rsidR=\"00BC116F\"><w:rPr><w:vanish/></w:rPr><w:instrText>LISTNUM\"listreset\"\\l1\\s0</w:instrText></w:r><w:r><w:rPr><w:vanish/></w:rPr><w:fldChar w:fldCharType=\"end\"/><wx:t wx:val=\" .\"/></w:r></w:p><w:sectPr wsp:rsidR=\"00B17B94\" wsp:rsidSect=\"007F3E1C\"><w:pgSz w:w=\"12240\" w:h=\"15840\" w:code=\"1\"/><w:pgMar w:top=\"1440\" w:right=\"1440\" w:bottom=\"1440\" w:left=\"1440\" w:header=\"432\" w:footer=\"432\" w:gutter=\"0\"/><w:pgNumType w:start=\"1\"/><w:cols w:space=\"475\"/><w:noEndnote/><w:docGrid w:line-pitch=\"360\"/></w:sectPr></w:body>";
	private final static String END_CHANGE_2007 ="<w:body><w:p wsp:rsidR=\"00682312\" wsp:rsidRDefault=\"00B17B94\"><w:hlink w:dest=\"http://none/edit?guid=A+7R6BYOEiR5BY230lgA&amp;branchId=2380\"><w:r wsp:rsidR=\"00455A9D\"><w:rPr><w:rStyle w:val=\"Hyperlink\"/></w:rPr><w:t>OSEE_EDIT_START</w:t></w:r></w:hlink></w:p><w:p wsp:rsidR=\"00B17B94\" wsp:rsidRDefault=\"00682312\"><w:r><w:t>End Change</w:t></w:r><w:hlink w:dest=\"http://none/edit?guid=A+7R6BYOEiR5BY230lgA&amp;branchId=2380\"><w:r wsp:rsidR=\"00455A9D\"><w:rPr><w:rStyle w:val=\"Hyperlink\"/></w:rPr><w:t>OSEE_EDIT_END</w:t></w:r></w:hlink></w:p><w:p wsp:rsidR=\"00B17B94\" wsp:rsidRDefault=\"00B17B94\"/><w:p wsp:rsidR=\"00B17B94\" wsp:rsidRDefault=\"00B17B94\"><w:pPr><w:rPr><w:vanish/></w:rPr></w:pPr><w:r><w:rPr><w:vanish/></w:rPr><w:fldChar w:fldCharType=\"begin\"/></w:r><w:r wsp:rsidR=\"00455A9D\"><w:rPr><w:vanish/></w:rPr><w:instrText>LISTNUM\"listreset\"\\l1\\s0</w:instrText></w:r><w:r><w:rPr><w:vanish/></w:rPr><w:fldChar w:fldCharType=\"end\"/><wx:t wx:val=\" .\"/></w:r></w:p><w:sectPr wsp:rsidR=\"00B17B94\" wsp:rsidSect=\"007F3E1C\"><w:pgSz w:w=\"12240\" w:h=\"15840\" w:code=\"1\"/><w:pgMar w:top=\"1440\" w:right=\"1440\" w:bottom=\"1440\" w:left=\"1440\" w:header=\"432\" w:footer=\"432\" w:gutter=\"0\"/><w:pgNumType w:start=\"1\"/><w:cols w:space=\"475\"/><w:noEndnote/><w:docGrid w:line-pitch=\"360\"/></w:sectPr></w:body>";
	private final static String CHANGE_IN_BETWEEN_2007_MULTI = "<w:body><wx:sub-section><w:p wsp:rsidR=\"007E22BA\" wsp:rsidRDefault=\"00D61766\"><w:pPr><w:pStyle w:val=\"Heading1\"/><w:listPr><wx:t wx:val=\"1.  \"/><wx:font wx:val=\"Times New Roman\"/></w:listPr></w:pPr><w:r><w:t>blah</w:t></w:r></w:p><w:p wsp:rsidR=\"007E22BA\" wsp:rsidRDefault=\"007E22BA\"><w:hlink w:dest=\"http://none/edit?guid=A+7R6BYOEiR5BY230lgA&amp;branchId=4059\"><w:r wsp:rsidR=\"00D61766\"><w:rPr><w:rStyle w:val=\"Hyperlink\"/></w:rPr><w:t>OSEE_EDIT_START</w:t></w:r></w:hlink></w:p><w:p wsp:rsidR=\"00AB7603\" wsp:rsidRDefault=\"00AB7603\"><w:r><w:t>One</w:t></w:r></w:p><w:p wsp:rsidR=\"007E22BA\" wsp:rsidRDefault=\"007E22BA\"><w:hlink w:dest=\"http://none/edit?guid=A+7R6BYOEiR5BY230lgA&amp;branchId=4059\"><w:r wsp:rsidR=\"00D61766\"><w:rPr><w:rStyle w:val=\"Hyperlink\"/></w:rPr><w:t>OSEE_EDIT_END</w:t></w:r></w:hlink></w:p><w:p wsp:rsidR=\"007E22BA\" wsp:rsidRDefault=\"007E22BA\"/><w:p wsp:rsidR=\"007E22BA\" wsp:rsidRDefault=\"007E22BA\"><w:pPr><w:rPr><w:vanish/></w:rPr></w:pPr><w:r><w:rPr><w:vanish/></w:rPr><w:fldChar w:fldCharType=\"begin\"/></w:r><w:r wsp:rsidR=\"00D61766\"><w:rPr><w:vanish/></w:rPr><w:instrText>LISTNUM\"listreset\"\\l1\\s0</w:instrText></w:r><w:r><w:rPr><w:vanish/></w:rPr><w:fldChar w:fldCharType=\"end\"/><wx:t wx:val=\" .\"/></w:r></w:p></wx:sub-section><wx:sub-section><w:p wsp:rsidR=\"007E22BA\" wsp:rsidRDefault=\"00D61766\"><w:pPr><w:pStyle w:val=\"Heading1\"/><w:listPr><wx:t wx:val=\"2.  \"/><wx:font wx:val=\"Times New Roman\"/></w:listPr></w:pPr><w:r><w:t>blah2</w:t></w:r></w:p><w:p wsp:rsidR=\"007E22BA\" wsp:rsidRDefault=\"007E22BA\"><w:hlink w:dest=\"http://none/edit?guid=A+8lBVkeBRM4X6ZFJ+gA&amp;branchId=4059\"><w:r wsp:rsidR=\"00D61766\"><w:rPr><w:rStyle w:val=\"Hyperlink\"/></w:rPr><w:t>OSEE_EDIT_START</w:t></w:r></w:hlink></w:p><w:p wsp:rsidR=\"00BB1E70\" wsp:rsidRPr=\"007F5EE9\" wsp:rsidRDefault=\"00AB7603\"><w:pPr><w:rPr><w:rFonts w:ascii=\"C39HrP48DlTt\" w:h-ansi=\"C39HrP48DlTt\"/><wx:font wx:val=\"C39HrP48DlTt\"/></w:rPr></w:pPr><w:r><w:t>Two</w:t></w:r></w:p><w:p wsp:rsidR=\"007E22BA\" wsp:rsidRDefault=\"007E22BA\"><w:hlink w:dest=\"http://none/edit?guid=A+8lBVkeBRM4X6ZFJ+gA&amp;branchId=4059\"><w:r wsp:rsidR=\"00D61766\"><w:rPr><w:rStyle w:val=\"Hyperlink\"/></w:rPr><w:t>OSEE_EDIT_END</w:t></w:r></w:hlink></w:p><w:p wsp:rsidR=\"007E22BA\" wsp:rsidRDefault=\"007E22BA\"/><w:p wsp:rsidR=\"007E22BA\" wsp:rsidRDefault=\"007E22BA\"><w:pPr><w:rPr><w:vanish/></w:rPr></w:pPr><w:r><w:rPr><w:vanish/></w:rPr><w:fldChar w:fldCharType=\"begin\"/></w:r><w:r wsp:rsidR=\"00D61766\"><w:rPr><w:vanish/></w:rPr><w:instrText>LISTNUM\"listreset\"\\l1\\s0</w:instrText></w:r><w:r><w:rPr><w:vanish/></w:rPr><w:fldChar w:fldCharType=\"end\"/><wx:t wx:val=\" .\"/></w:r></w:p></wx:sub-section><w:sectPr wsp:rsidR=\"007E22BA\" wsp:rsidSect=\"007F3E1C\"><w:pgSz w:w=\"12240\" w:h=\"15840\" w:code=\"1\"/><w:pgMar w:top=\"1440\" w:right=\"1440\" w:bottom=\"1440\" w:left=\"1440\" w:header=\"432\" w:footer=\"432\" w:gutter=\"0\"/><w:pgNumType w:start=\"1\"/><w:cols w:space=\"475\"/><w:noEndnote/><w:docGrid w:line-pitch=\"360\"/></w:sectPr></w:body>";
	
	private final static String CHANGE_IN_BETWEEN_2003 = "<w:body><wx:sect><w:p wsp:rsidR=\"00854E9E\" wsp:rsidRDefault=\"00854E9E\"><w:hlink w:dest=\"http://none/edit?guid=AZkaMmpE_AJAtatzUqwA&amp;branchId=2315\"><w:r wsp:rsidR=\"00763F1D\"><w:rPr><w:rStyle w:val=\"Hyperlink\"/></w:rPr><w:t>OSEE_EDIT_START</w:t></w:r></w:hlink></w:p><w:p wsp:rsidR=\"00D83EA2\" wsp:rsidRDefault=\"00763F1D\"><w:r><w:t>This is a test</w:t></w:r><w:r wsp:rsidR=\"00C4548D\"><w:t>-x</w:t></w:r></w:p><w:p wsp:rsidR=\"00854E9E\" wsp:rsidRDefault=\"00854E9E\"><w:hlink w:dest=\"http://none/edit?guid=AZkaMmpE_AJAtatzUqwA&amp;branchId=2315\"><w:r wsp:rsidR=\"00763F1D\"><w:rPr><w:rStyle w:val=\"Hyperlink\"/></w:rPr><w:t>OSEE_EDIT_END</w:t></w:r></w:hlink></w:p><w:p wsp:rsidR=\"00854E9E\" wsp:rsidRDefault=\"00854E9E\"/><w:p wsp:rsidR=\"00854E9E\" wsp:rsidRDefault=\"00854E9E\"><w:pPr><w:rPr><w:vanish/></w:rPr></w:pPr><w:r><w:rPr><w:vanish/></w:rPr><w:fldChar w:fldCharType=\"begin\"/></w:r><w:r wsp:rsidR=\"00763F1D\"><w:rPr><w:vanish/></w:rPr><w:instrText>LISTNUM\"listreset\"\\l1\\s0</w:instrText></w:r><w:r><w:rPr><w:vanish/></w:rPr><w:fldChar w:fldCharType=\"end\"/><wx:t wx:val=\" .\"/></w:r></w:p><w:sectPr wsp:rsidR=\"00854E9E\" wsp:rsidSect=\"007F3E1C\"><w:pgSz w:w=\"12240\" w:h=\"15840\" w:code=\"1\"/><w:pgMar w:top=\"1440\" w:right=\"1440\" w:bottom=\"1440\" w:left=\"1440\" w:header=\"432\" w:footer=\"432\" w:gutter=\"0\"/><w:pgNumType w:start=\"1\"/><w:cols w:space=\"475\"/><w:noEndnote/></w:sectPr></wx:sect></w:body>";
	private final static String START_END_CHANGE_2003 = "<w:body><wx:sect><w:p wsp:rsidR=\"00235CEA\" wsp:rsidRDefault=\"00474817\"><w:r><w:t>Not_this</w:t></w:r><w:hlink w:dest=\"http://none/edit?guid=AZkaMmpE_AJAtatzUqwA&amp;branchId=2315\"><w:r wsp:rsidR=\"00E00A43\"><w:rPr><w:rStyle w:val=\"Hyperlink\"/></w:rPr><w:t>OSEE_EDIT_START</w:t></w:r></w:hlink><w:r><w:t>this</w:t></w:r></w:p><w:p wsp:rsidR=\"00D83EA2\" wsp:rsidRDefault=\"00E00A43\"><w:r><w:t>This is a test</w:t></w:r></w:p><w:p wsp:rsidR=\"00235CEA\" wsp:rsidRDefault=\"00474817\"><w:r><w:t>that</w:t></w:r><w:hlink w:dest=\"http://none/edit?guid=AZkaMmpE_AJAtatzUqwA&amp;branchId=2315\"><w:r wsp:rsidR=\"00E00A43\"><w:rPr><w:rStyle w:val=\"Hyperlink\"/></w:rPr><w:t>OSEE_EDIT_END</w:t></w:r></w:hlink><w:r><w:t>not_that</w:t></w:r></w:p><w:p wsp:rsidR=\"00235CEA\" wsp:rsidRDefault=\"00235CEA\"/><w:p wsp:rsidR=\"00235CEA\" wsp:rsidRDefault=\"00235CEA\"><w:pPr><w:rPr><w:vanish/></w:rPr></w:pPr><w:r><w:rPr><w:vanish/></w:rPr><w:fldChar w:fldCharType=\"begin\"/></w:r><w:r wsp:rsidR=\"00E00A43\"><w:rPr><w:vanish/></w:rPr><w:instrText>LISTNUM\"listreset\"\\l1\\s0</w:instrText></w:r><w:r><w:rPr><w:vanish/></w:rPr><w:fldChar w:fldCharType=\"end\"/><wx:t wx:val=\" .\"/></w:r></w:p><w:sectPr wsp:rsidR=\"00235CEA\" wsp:rsidSect=\"007F3E1C\"><w:pgSz w:w=\"12240\" w:h=\"15840\" w:code=\"1\"/><w:pgMar w:top=\"1440\" w:right=\"1440\" w:bottom=\"1440\" w:left=\"1440\" w:header=\"432\" w:footer=\"432\" w:gutter=\"0\"/><w:pgNumType w:start=\"1\"/><w:cols w:space=\"475\"/><w:noEndnote/></w:sectPr></wx:sect></w:body>";
	private final static String CHANGE_IN_BETWEEN_2003_MULTI = "<w:body><wx:sect><wx:sub-section><w:p wsp:rsidR=\"005F4166\" wsp:rsidRDefault=\"00122167\"><w:pPr><w:pStyle w:val=\"Heading1\"/><w:listPr><wx:t wx:val=\"1.  \"/><wx:font wx:val=\"Times New Roman\"/></w:listPr></w:pPr><w:r><w:t>Tag Test</w:t></w:r></w:p><w:p wsp:rsidR=\"005F4166\" wsp:rsidRDefault=\"005F4166\"><w:hlink w:dest=\"http://none/edit?guid=AZkaMmpE_AJAtatzUqwA&amp;branchId=2315\"><w:r wsp:rsidR=\"00122167\"><w:rPr><w:rStyle w:val=\"Hyperlink\"/></w:rPr><w:t>OSEE_EDIT_START</w:t></w:r></w:hlink></w:p><w:p wsp:rsidR=\"00D83EA2\" wsp:rsidRDefault=\"00122167\"><w:r><w:t>This is a test</w:t></w:r><w:r wsp:rsidR=\"00A4341C\"><w:t>-x</w:t></w:r></w:p><w:p wsp:rsidR=\"005F4166\" wsp:rsidRDefault=\"005F4166\"><w:hlink w:dest=\"http://none/edit?guid=AZkaMmpE_AJAtatzUqwA&amp;branchId=2315\"><w:r wsp:rsidR=\"00122167\"><w:rPr><w:rStyle w:val=\"Hyperlink\"/></w:rPr><w:t>OSEE_EDIT_END</w:t></w:r></w:hlink></w:p><w:p wsp:rsidR=\"005F4166\" wsp:rsidRDefault=\"005F4166\"/><w:p wsp:rsidR=\"005F4166\" wsp:rsidRDefault=\"005F4166\"><w:pPr><w:rPr><w:vanish/></w:rPr></w:pPr><w:r><w:rPr><w:vanish/></w:rPr><w:fldChar w:fldCharType=\"begin\"/></w:r><w:r wsp:rsidR=\"00122167\"><w:rPr><w:vanish/></w:rPr><w:instrText>LISTNUM\"listreset\"\\l1\\s0</w:instrText></w:r><w:r><w:rPr><w:vanish/></w:rPr><w:fldChar w:fldCharType=\"end\"/><wx:t wx:val=\" .\"/></w:r></w:p></wx:sub-section><wx:sub-section><w:p wsp:rsidR=\"005F4166\" wsp:rsidRDefault=\"00122167\"><w:pPr><w:pStyle w:val=\"Heading1\"/><w:listPr><wx:t wx:val=\"2.  \"/><wx:font wx:val=\"Times New Roman\"/></w:listPr></w:pPr><w:r><w:t>Tag Test 2</w:t></w:r></w:p><w:p wsp:rsidR=\"005F4166\" wsp:rsidRDefault=\"005F4166\"><w:hlink w:dest=\"http://none/edit?guid=AZkj9HgJnT38eBT3jiAA&amp;branchId=2315\"><w:r wsp:rsidR=\"00122167\"><w:rPr><w:rStyle w:val=\"Hyperlink\"/></w:rPr><w:t>OSEE_EDIT_START</w:t></w:r></w:hlink></w:p><w:p wsp:rsidR=\"00500093\" wsp:rsidRDefault=\"00122167\"><w:r><w:t>This has a newline</w:t></w:r><w:r wsp:rsidR=\"00A4341C\"><w:t>-y</w:t></w:r></w:p><w:p wsp:rsidR=\"00001CCC\" wsp:rsidRDefault=\"00122167\"/><w:p wsp:rsidR=\"005F4166\" wsp:rsidRDefault=\"005F4166\"><w:hlink w:dest=\"http://none/edit?guid=AZkj9HgJnT38eBT3jiAA&amp;branchId=2315\"><w:r wsp:rsidR=\"00122167\"><w:rPr><w:rStyle w:val=\"Hyperlink\"/></w:rPr><w:t>OSEE_EDIT_END</w:t></w:r></w:hlink></w:p><w:p wsp:rsidR=\"005F4166\" wsp:rsidRDefault=\"005F4166\"/><w:p wsp:rsidR=\"005F4166\" wsp:rsidRDefault=\"005F4166\"><w:pPr><w:rPr><w:vanish/></w:rPr></w:pPr><w:r><w:rPr><w:vanish/></w:rPr><w:fldChar w:fldCharType=\"begin\"/></w:r><w:r wsp:rsidR=\"00122167\"><w:rPr><w:vanish/></w:rPr><w:instrText>LISTNUM\"listreset\"\\l1\\s0</w:instrText></w:r><w:r><w:rPr><w:vanish/></w:rPr><w:fldChar w:fldCharType=\"end\"/><wx:t wx:val=\" .\"/></w:r></w:p><w:sectPr wsp:rsidR=\"005F4166\" wsp:rsidSect=\"007F3E1C\"><w:pgSz w:w=\"12240\" w:h=\"15840\" w:code=\"1\"/><w:pgMar w:top=\"1440\" w:right=\"1440\" w:bottom=\"1440\" w:left=\"1440\" w:header=\"432\" w:footer=\"432\" w:gutter=\"0\"/><w:pgNumType w:start=\"1\"/><w:cols w:space=\"475\"/><w:noEndnote/></w:sectPr></wx:sub-section></wx:sect></w:body>";

	@org.junit.Test
	public void testEmptyChange() throws Exception {
		WordArtifactElementExtractor artifactElementExtractor = new WordArtifactElementExtractor(
				getDocument(NO_CHANGE));
		try{
			artifactElementExtractor.extractElements();
			assert(false);
		}catch(OseeCoreException ex){
			//We expected to throw an exception
		}
	}

	@org.junit.Test
	public void testInBetweenChange2007() throws Exception {
		WordArtifactElementExtractor artifactElementExtractor = new WordArtifactElementExtractor(
				getDocument(CHANGE_IN_BETWEEN_2007));

		Collection<Element> artElements = artifactElementExtractor.extractElements();
		assertTrue(artifactElementExtractor.extractElements().size() == 1);

		for(Element artelement : artElements){
			assertTrue("Middle change".equals(WordUtil.textOnly(
					Lib.inputStreamToString(new ByteArrayInputStream(
							WordTemplateRenderer.getFormattedContent(artelement))))));
		}
	}
	
	@org.junit.Test
	public void testStartChange2007() throws Exception {
		WordArtifactElementExtractor artifactElementExtractor = new WordArtifactElementExtractor(
				getDocument(START_CHANGE_2007));

		Collection<Element> artElements = artifactElementExtractor.extractElements();
		assertTrue(artifactElementExtractor.extractElements().size() == 1);

		for(Element artelement : artElements){
			assertTrue("I am change".equals(WordUtil.textOnly(
					Lib.inputStreamToString(new ByteArrayInputStream(
							WordTemplateRenderer.getFormattedContent(artelement))))));
		}
	}

	@org.junit.Test
	public void testEndChange2007() throws Exception {
		WordArtifactElementExtractor artifactElementExtractor = new WordArtifactElementExtractor(
				getDocument(END_CHANGE_2007));

		Collection<Element> artElements = artifactElementExtractor.extractElements();
		assertTrue(artifactElementExtractor.extractElements().size() == 1);

		for(Element artelement : artElements){
			assertTrue("End Change".equals(WordUtil.textOnly(
					Lib.inputStreamToString(new ByteArrayInputStream(
							WordTemplateRenderer.getFormattedContent(artelement))))));
		}
	}
	
	@org.junit.Test
	public void testInBetweenChange2007Multi() throws Exception {
		WordArtifactElementExtractor artifactElementExtractor = new WordArtifactElementExtractor(
				getDocument(CHANGE_IN_BETWEEN_2007_MULTI));

		List<Element> artElements = artifactElementExtractor.extractElements();
		assertTrue("expected 2 got " + artifactElementExtractor.extractElements().size(), artifactElementExtractor.extractElements().size() == 2);

		List<String> testText = Arrays.asList("One", "Two");

		for(int i = 0; i < artElements.size() ; i++){
			String artContent = WordUtil.textOnly(
					Lib.inputStreamToString(new ByteArrayInputStream(
							WordTemplateRenderer.getFormattedContent(artElements.get(i)))));
			assertTrue("expected:*"+testText.get(i)+"* got:*"+ artContent + "*", testText.get(i).equals(artContent));
		}

	}
	
	@org.junit.Test
	public void testInBetweenChange2003() throws Exception {
		WordArtifactElementExtractor artifactElementExtractor = new WordArtifactElementExtractor(
				getDocument(CHANGE_IN_BETWEEN_2003));

		Collection<Element> artElements = artifactElementExtractor.extractElements();
		assertTrue(artifactElementExtractor.extractElements().size() == 1);

		for(Element artelement : artElements){
			assertTrue("This is a test-x".equals(WordUtil.textOnly(
					Lib.inputStreamToString(new ByteArrayInputStream(
							WordTemplateRenderer.getFormattedContent(artelement))))));
		}
	}
	
	@org.junit.Test
	public void testStartEndChange2003() throws Exception {
		WordArtifactElementExtractor artifactElementExtractor = new WordArtifactElementExtractor(
				getDocument(START_END_CHANGE_2003));

		Collection<Element> artElements = artifactElementExtractor.extractElements();
		assertTrue(artifactElementExtractor.extractElements().size() == 1);

		for(Element artelement : artElements){
			String artContent = WordUtil.textOnly(
					Lib.inputStreamToString(new ByteArrayInputStream(
							WordTemplateRenderer.getFormattedContent(artelement))));
			assertTrue("Got*"+artContent, "this This is a test that".equals(artContent));
		}
	}

	@org.junit.Test
	public void testInBetweenChange2003Multi() throws Exception {
		WordArtifactElementExtractor artifactElementExtractor = new WordArtifactElementExtractor(
				getDocument(CHANGE_IN_BETWEEN_2003_MULTI));

		List<Element> artElements = artifactElementExtractor.extractElements();
		assertTrue("expected 2 got " + artifactElementExtractor.extractElements().size(), artifactElementExtractor.extractElements().size() == 2);

		List<String> testText = Arrays.asList("This is a test-x", "This has a newline-y");

		for(int i = 0; i < artElements.size() ; i++){
			String artContent = WordUtil.textOnly(
					Lib.inputStreamToString(new ByteArrayInputStream(
							WordTemplateRenderer.getFormattedContent(artElements.get(i)))));
			assertTrue("expected:*"+testText.get(i)+"* got:*"+ artContent + "*", testText.get(i).equals(artContent));
		}

	}

	private Document getDocument(String string)
	throws ParserConfigurationException, SAXException, IOException {
		Document document;
		InputStream inputStream = new ByteArrayInputStream(
				string.getBytes("UTF-8"));
		try {
			document = Jaxp.readXmlDocument(inputStream);
		} finally {
			Lib.close(inputStream);
		}
		return document;
	}
}
