/*******************************************************************************
 * Copyright (c) 2004, 2007 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.client.integration.tests.integration.skynet.core;

import static org.eclipse.osee.client.demo.DemoChoice.OSEE_CLIENT_DEMO;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.osee.client.test.framework.OseeClientIntegrationRule;
import org.eclipse.osee.client.test.framework.OseeLogMonitorRule;
import org.eclipse.osee.client.test.framework.TestInfo;
import org.eclipse.osee.framework.core.data.IOseeBranch;
import org.eclipse.osee.framework.core.enums.CoreArtifactTypes;
import org.eclipse.osee.framework.core.enums.CoreAttributeTypes;
import org.eclipse.osee.framework.core.enums.DeletionFlag;
import org.eclipse.osee.framework.core.exception.ArtifactDoesNotExist;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.model.Branch;
import org.eclipse.osee.framework.core.model.TransactionRecord;
import org.eclipse.osee.framework.core.model.cache.BranchFilter;
import org.eclipse.osee.framework.jdk.core.util.GUID;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.ArtifactCache;
import org.eclipse.osee.framework.skynet.core.artifact.ArtifactTypeManager;
import org.eclipse.osee.framework.skynet.core.artifact.BranchManager;
import org.eclipse.osee.framework.skynet.core.artifact.search.ArtifactQuery;
import org.eclipse.osee.framework.skynet.core.artifact.search.ArtifactSearchCriteria;
import org.eclipse.osee.framework.skynet.core.artifact.search.AttributeCriteria;
import org.eclipse.osee.framework.skynet.core.artifact.search.QueryOptions;
import org.eclipse.osee.framework.skynet.core.transaction.TransactionManager;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Donald G. Dunne
 */
public class ArtifactQueryTest {

   @Rule
   public OseeClientIntegrationRule integration = new OseeClientIntegrationRule(OSEE_CLIENT_DEMO);

   @Rule
   public OseeLogMonitorRule monitorRule = new OseeLogMonitorRule();

   @Rule
   public TestInfo testInfo = new TestInfo();

   @Test
   public void testGetArtifactFromGUIDDeleted() throws OseeCoreException {
      Artifact newArtifact =
         ArtifactTypeManager.addArtifact(CoreArtifactTypes.GeneralData, BranchManager.getCommonBranch());
      newArtifact.persist(getClass().getSimpleName());

      // Should exist
      Artifact searchedArtifact =
         ArtifactQuery.getArtifactFromId(newArtifact.getGuid(), BranchManager.getCommonBranch());
      Assert.assertNotNull(searchedArtifact);

      // Should exist with allowDeleted
      searchedArtifact =
         ArtifactQuery.getArtifactFromId(newArtifact.getGuid(), BranchManager.getCommonBranch(),
            DeletionFlag.INCLUDE_DELETED);
      Assert.assertNotNull(searchedArtifact);

      newArtifact.deleteAndPersist();

      try {
         Artifact ret = ArtifactQuery.checkArtifactFromId(newArtifact.getGuid(), BranchManager.getCommonBranch());
         Assert.assertNull(ret);
      } catch (ArtifactDoesNotExist ex) {
         Assert.fail("ArtifactQuery should never throw ArtifactDoesNotExist with QueryType.CHECK");
      }

      // Should NOT exist, cause deleted
      try {
         ArtifactQuery.getArtifactFromId(newArtifact.getGuid(), BranchManager.getCommonBranch());
         Assert.fail("artifact query should have thrown does not exist exception");
      } catch (ArtifactDoesNotExist ex) {
         // do nothing, this is the expected case
      }

      // Should still exist with allowDeleted
      searchedArtifact =
         ArtifactQuery.getArtifactFromId(newArtifact.getGuid(), BranchManager.getCommonBranch(),
            DeletionFlag.INCLUDE_DELETED);
      Assert.assertNotNull(searchedArtifact);

   }

   @Test
   public void testGetArtifactListFromType() throws OseeCoreException {
      // Should exist
      Set<Artifact> searchedArtifacts = new LinkedHashSet<Artifact>();
      for (IOseeBranch branch : BranchManager.getBranches(new BranchFilter())) {
         searchedArtifacts.addAll(ArtifactQuery.getArtifactListFromType(CoreArtifactTypes.SoftwareRequirement, branch,
            DeletionFlag.INCLUDE_DELETED));
      }
      // make sure at least one artifact exists
      Assert.assertTrue("No artifacts found", searchedArtifacts.size() > 0);

      //check to see if there are multiple branches found
      String firstGuid = "";
      Boolean pass = false;
      for (Artifact a : searchedArtifacts) {
         if ("" == firstGuid) {
            firstGuid = a.getBranchGuid();
         } else {
            if (firstGuid != a.getBranchGuid()) {
               pass = true;
               break;
            }
         }
      }
      Assert.assertTrue("No artifacts on multiple branches found", pass);
   }

   @Test
   public void testNotTaggableGetArtifactListFromAttributeType() {
      List<ArtifactSearchCriteria> criteria = new ArrayList<ArtifactSearchCriteria>();

      criteria.add(new AttributeCriteria(CoreAttributeTypes.FavoriteBranch, "Common"));
      // test against a couple of attributes types that are not taggable; expect exception
      try {
         List<Artifact> artifacts =
            ArtifactQuery.getArtifactListFromCriteria(BranchManager.getCommonBranch(), 1000, criteria);
         Assert.fail("Should have thrown an exception as the attribute type are not taggable");
      } catch (OseeCoreException e) {
         Assert.assertTrue(e.getMessage(), Boolean.TRUE);
      }

      try {
         List<Artifact> artifacts =
            ArtifactQuery.getArtifactListFromTypeAndAttribute(CoreArtifactTypes.User, CoreAttributeTypes.Active,
               "true", BranchManager.getCommonBranch());
         Assert.fail("Should have thrown an exception as the attribute type are not taggable");
      } catch (OseeCoreException e) {
         Assert.assertTrue(e.getMessage(), Boolean.TRUE);
      }
      // test against a couple attributes types that are taggable; do not expect exception
      criteria.clear();
      criteria.add(new AttributeCriteria(CoreAttributeTypes.Email, "john.doe@somewhere.com"));
      try {
         List<Artifact> artifacts =
            ArtifactQuery.getArtifactListFromCriteria(BranchManager.getCommonBranch(), 1000, criteria);
         Assert.assertTrue("This attribute type is taggable", Boolean.TRUE);
      } catch (OseeCoreException e) {
         Assert.fail(e.getMessage());
      }

      try {
         List<Artifact> artifacts =
            ArtifactQuery.getArtifactListFromTypeAndAttribute(CoreArtifactTypes.User, CoreAttributeTypes.Notes,
               "My Notes", BranchManager.getCommonBranch());
         Assert.assertTrue("This attribute type is taggable", Boolean.TRUE);
      } catch (OseeCoreException e) {
         Assert.fail(e.getMessage());
      }

   }

   @Test
   public void testGetOrCreate() throws Exception {
      String guid = GUID.create();
      Branch branch = BranchManager.createTopLevelBranch(testInfo.getTestName() + " branch");
      Artifact artifact1 = ArtifactQuery.getOrCreate(guid, null, CoreArtifactTypes.GeneralData, branch);
      Assert.assertNotNull(artifact1);
      Artifact artifact2 = ArtifactQuery.getOrCreate(guid, null, CoreArtifactTypes.GeneralData, branch);
      Assert.assertEquals(artifact1, artifact2);
      Job job = BranchManager.deleteBranch(branch);
      job.join();
   }

   @Test
   public void testLargeAttributeIndexing() throws Exception {
      String guid = GUID.create();
      Branch branch = BranchManager.createTopLevelBranch(testInfo.getTestName() + " branch");
      Artifact artifact1 = ArtifactQuery.getOrCreate(guid, null, CoreArtifactTypes.GeneralData, branch);
      artifact1.setSoleAttributeFromString(CoreAttributeTypes.Name, longStr());
      artifact1.persist(testInfo.getTestName());
      Thread.sleep(1000);
      List<Artifact> artifacts =
         ArtifactQuery.getArtifactListFromName("Wikipedia", branch, DeletionFlag.EXCLUDE_DELETED,
            QueryOptions.CONTAINS_MATCH_OPTIONS);
      Job job = BranchManager.deleteBranch(branch);
      job.join();
      Assert.assertEquals(1, artifacts.size());
      Assert.assertEquals(artifact1, artifacts.iterator().next());
   }

   @Test
   public void testQueryById() throws OseeCoreException {
      Branch branch = BranchManager.createTopLevelBranch(testInfo.getTestName() + " branch");

      List<Integer> newIdsInOrder = new LinkedList<Integer>();
      Map<Integer, TransactionRecord> idToTxId = new HashMap<Integer, TransactionRecord>();
      //create 3 artifacts, decache them
      for (int i = 0; i < 2; i++) {
         Artifact created = ArtifactTypeManager.addArtifact(CoreArtifactTypes.Folder, branch);
         created.persist(testInfo.getTestName());
         ArtifactCache.deCache(created);
         newIdsInOrder.add(created.getArtId());
         TransactionRecord tx = TransactionManager.getHeadTransaction(branch);
         idToTxId.put(created.getArtId(), tx);
      }

      Assert.assertEquals(2, newIdsInOrder.size());

      //create a new tx deleting the first created
      Artifact firstCreated = ArtifactQuery.getArtifactFromId(newIdsInOrder.get(0), branch);
      firstCreated.deleteAndPersist();
      ArtifactCache.deCache(firstCreated);

      Artifact toCheck = ArtifactQuery.checkArtifactFromId(newIdsInOrder.get(0), branch, DeletionFlag.EXCLUDE_DELETED);
      Assert.assertNull(toCheck);
      toCheck = ArtifactQuery.checkArtifactFromId(newIdsInOrder.get(0), branch, DeletionFlag.INCLUDE_DELETED);
      Assert.assertNotNull(toCheck);
      ArtifactCache.deCache(toCheck);

      TransactionRecord beforeDelete = idToTxId.get(newIdsInOrder.get(1));
      Assert.assertNotNull(ArtifactQuery.checkHistoricalArtifactFromId(firstCreated.getArtId(), beforeDelete,
         DeletionFlag.EXCLUDE_DELETED));
   }

   private String longStr() {
      return "Wiki From Wikipedia, the free encyclopedia Jump to: navigation, search This article is about the type of website. For other uses, see Wiki (disambiguation).  Not to be confused with Wikipedia.  Edit summary redirects here. For edit summaries as used in Wikipedia, see Help:Edit summary.  WikiNode redirects here. For the WikiNode of Wikipedia, see Wikipedia:WikiNode.  Page semi-protected Ward Cunningham, inventor of the wiki A wiki (Listeni/'w?ki?/ WIK-ee) is a website which allows its users to add, modify, or delete its content via a web browser usually using a simplified markup language or a rich-text editor.[1][2][3] Wikis are powered by wiki software. Most are created collaboratively.  Wikis serve many different purposes, such as knowledge management and notetaking. Wikis can be community websites and intranets, for example. Some permit control over different functions (levels of access). For example, editing rights may permit changing, adding or removing material. Others may permit access without enforcing access control. Other rules may also be imposed for organizing content.  Ward Cunningham, the developer of the first wiki software, WikiWikiWeb, originally described it as the simplest online database that could possibly work.[4] Wiki (pronounced ['witi] or ['viti]) is a Hawaiian word meaning fast or quick.[5] Contents 1 Characteristics 1.1 Editing wiki pages 1.2 Navigation 1.3 Linking and creating pages 1.4 Searching 2 History 3 Implementations 4 Trust and security 4.1 Controlling changes 4.2 Trustworthiness 4.3 Security 4.3.1 Potential malware vector 5 Communities 5.1 Growth factors 6 Conferences 7 Rules 8 Legal environment 9 See also 10 References 11 Further reading 12 External links Characteristics Ward Cunningham and co-author Bo Leuf, in their book The Wiki Way: Quick Collaboration on the Web, described the essence of the Wiki concept as follows: A wiki invites all users to edit any page or to create new pages within the wiki Web site, using only a plain-vanilla Web browser without any extra add-ons.  Wiki promotes meaningful topic associations between different pages by making page link creation almost intuitively easy and showing whether an intended target page exists or not.  A wiki is not a carefully crafted site for casual visitors. Instead, it seeks to involve the visitor in an ongoing process of creation and collaboration that constantly changes the Web site landscape.  A wiki enables communities to write documents collaboratively, using a simple markup language and a web browser. A single page in a wiki website is referred to as a wiki page, while the entire collection of pages, which are usually well interconnected by hyperlinks, is the wiki. A wiki is essentially a database for creating, browsing, and searching through information. A wiki allows for non-linear, evolving, complex and networked text, argument and interaction.[6] A defining characteristic of wiki technology is the ease with which pages can be created and updated. Generally, there is no review before modifications are accepted. Many wikis are open to alteration by the general public without requiring them to register user accounts. Many edits can be made in real-time and appear almost instantly online. This can facilitate abuse of the system. Private wiki servers require user authentication to edit pages, and sometimes even to read them.  Maged N. Kamel Boulos, Cito Maramba and Steve Wheeler write that it is the openness of wikis that gives rise to the concept of 'Darwikinism', which is a concept that describes the 'socially Darwinian process' that wiki pages are subject to. Basically, because of the openness of wikis and the rapidity with which wiki pages can be edited, the pages undergo a natural selection process like that which nature subjects to living organisms. 'Unfit' sentences and sections are ruthlessly culled, edited and replaced if they are not considered 'fit', which hopefully results in the evolution of a higher quality and more relevant page. Whilst such openness may invite 'vandalism' and the posting of untrue information, this same openness also makes it possible to rapidly correct or restore a 'quality' wiki page.[7] Editing wiki pages There are many different ways in which wikis have users edit the content. Ordinarily, the structure and formatting of wiki pages are specified with a simplified markup language, sometimes known as wikitext (for example, starting a line of text with an asterisk often sets up a bulleted list). The style and syntax of wikitexts can vary greatly among wiki implementations, some of which also allow HTML tags. Designers of wikis often take this approach because HTML, with its many cryptic tags, is not very legible, making it hard to edit. Wikis therefore favour plain-text editing, with fewer and simpler conventions than HTML, for indicating style and structure. Although limiting access to HTML and Cascading Style Sheets (CSS) of wikis limits user ability to alter the structure and formatting of wiki content, there are some benefits. Limited access to CSS promotes consistency in the look and feel, and having JavaScript disabled prevents a user from implementing code that may limit access for other users.  MediaWiki syntax Equivalent HTML Rendered output Take some more [[tea]], the March Hare said to Alice, very earnestly.  I've had '''nothing''' yet, Alice replied in an offended tone, so I can't take more. You mean you can't take ''less''? said the Hatter. It's very easy to take ''more'' than nothing. <p>Take some more <a href=/wiki/Tea title=Tea>tea</a>, the March Hare said to Alice, very earnestly.</p> <p>I've had <b>nothing</b> yet, Alice replied in an offended tone, so I can't take more.</p> <p>You mean you can't take <i>less</i>? said the Hatter. It's very easy to take <i>more</i> than nothing.</p> Take some more tea, the March Hare said to Alice, very earnestly.  I've had nothing yet, Alice replied in an offended tone, so I can't take more. You mean you can't take less? said the Hatter. It's very easy to take more than nothing. Increasingly, wikis are making WYSIWYG editing available to users, usually by means of JavaScript or an ActiveX control that translates graphically entered formatting instructions into the corresponding HTML tags or wikitext. In those implementations, the markup of a newly edited, marked-up version of the page is generated and submitted to the server transparently, shielding the user from this technical detail. However, WYSIWYG controls do not always provide all of the features available in wikitext, and some users prefer not to use a WYSIWYG editor. Hence, many of these sites offer some means to edit the wikitext directly.  Most wikis keep a record of changes made to wiki pages; often, every version of the page is stored. This means that authors can revert to an older version of the page, should it be necessary because a mistake has been made or the page has been vandalized. Many implementations, like MediaWiki, allow users to supply an edit summary when they edit a page; this is a short piece of text summarising the changes. It is not inserted into the article, but is stored along with that revision of the page, allowing users to explain what has been done and why; this is similar to a log message when making changes to a revision-control system.  Navigation Within the text of most pages there are usually a large number of hypertext links to other pages. This form of non-linear navigation is more native to wiki than structured/formalized navigation schemes. That said, users can also create any number of index or table-of-contents pages, with hierarchical categorization or whatever form of organization they like. These may be challenging to maintain by hand, as multiple authors create and delete pages in an ad hoc manner. Wikis generally provide one or more ways to categorize or tag pages to support the maintenance of such index pages.  Most wikis have a backlink feature, which displays all pages that link to a given page.  It is typical in a wiki to create links to pages that do not yet exist, as a way to invite others to share what they know about a subject new to the wiki.  Linking and creating pages Links are created using a specific syntax, the so-called link pattern (also see CURIE). Originally, most wikis used CamelCase to name pages and create links. These are produced by capitalizing words in a phrase and removing the spaces between them (the word CamelCase is itself an example). While CamelCase makes linking very easy, it also leads to links which are written in a form that deviates from the standard spelling. To link to a page with a single-word title, one must abnormally capitalize one of the letters in the word (e.g. WiKi instead of Wiki). CamelCase-based wikis are instantly recognizable because they have many links with names such as TableOfContents and BeginnerQuestions. It is possible for a wiki to render the visible anchor for such links pretty by reinserting spaces, and possibly also reverting to lower case. However, this reprocessing of the link to improve the readability of the anchor is limited by the loss of capitalization information caused by CamelCase reversal. For example, RichardWagner should be rendered as Richard Wagner, whereas PopularMusic should be rendered as popular music. There is no easy way to determine which capital letters should remain capitalized. As a result, many wikis now have free linking using brackets, and some disable CamelCase by default.  Searching Most wikis offer at least a title search, and sometimes a full-text search. The scalability of the search depends on whether the wiki engine uses a database. Some wikis, such as PmWiki, use flat files.[8] MediaWiki's first versions used flat files, but it was rewritten by Lee Daniel Crocker in the early 2000s to be a database application. Indexed database access is necessary for high speed searches on large wikis. Alternatively, external search engines such as Google Search can sometimes be used on wikis with limited searching functions in order to obtain more precise results. However, a search engine's indexes can be very out of date (days, weeks or months) for many websites.  History Main article: History of wikis Wiki Wiki Shuttle at Honolulu International Airport WikiWikiWeb was the first wiki.[9] Ward Cunningham started developing WikiWikiWeb in Portland, Oregon, in 1994, and installed it on the Internet domain c2.com on March 25, 1995. It was named by Cunningham, who remembered a Honolulu International Airport counter employee telling him to take the Wiki Wiki Shuttle bus that runs between the airport's terminals. According to Cunningham, I chose wiki-wiki as an alliterative substitute for 'quick' and thereby avoided naming this stuff quick-web.[10][11] Cunningham was in part inspired by Apple's HyperCard. Apple had designed a system allowing users to create virtual card stacks supporting links among the various cards. Cunningham developed Vannevar Bush's ideas by allowing users to comment on and change one another's text.[2][12] In the early 2000s, wikis were increasingly adopted in enterprise as collaborative software. Common uses included project communication, intranets, and documentation, initially for technical users. Today some companies use wikis as their only collaborative software and as a replacement for static intranets, and some schools and universities use wikis to enhance group learning. There may be greater use of wikis behind firewalls than on the public Internet.  On March 15, 2007, wiki entered the online Oxford English Dictionary.[13] Implementations Wiki software is a type of collaborative software that runs a wiki system, allowing web pages to be created and edited using a common web browser. It is usually implemented as an application server[citation needed] that runs on one or more web servers. The content is stored in a file system, and changes to the content are stored in a relational database management system. A commonly implemented software package is MediaWiki, which runs this encyclopedia. See the List of wiki software for further information. Alternatively, personal wikis run as a standalone application on a single computer. WikidPad is an example. Or even single local HTML file with JavaScript inside  like TiddlyWiki.  Wikis can also be created on a wiki farm, where the server side software is implemented by the wiki farm owner. PBwiki, Socialtext, Wetpaint, and Wikia are popular examples of such services. Some wiki farms can also make private, password-protected wikis. Note that free wiki farms generally contain advertising on every page. For";
   }

}