package org.eclipse.osee.ats.core.review;

import java.util.List;
import org.eclipse.osee.framework.core.util.WorkPageAdapter;
import org.eclipse.osee.framework.core.util.WorkPageType;

public class PeerToPeerReviewState extends WorkPageAdapter {
   public static PeerToPeerReviewState Prepare = new PeerToPeerReviewState("Prepare", WorkPageType.Working);
   public static PeerToPeerReviewState Review = new PeerToPeerReviewState("Review", WorkPageType.Working);
   public static PeerToPeerReviewState Meeting = new PeerToPeerReviewState("Meeting", WorkPageType.Working);
   public static PeerToPeerReviewState Completed = new PeerToPeerReviewState("Completed", WorkPageType.Completed);

   private PeerToPeerReviewState(String pageName, WorkPageType workPageType) {
      super(PeerToPeerReviewState.class, pageName, workPageType);
   }

   public static PeerToPeerReviewState valueOf(String pageName) {
      return WorkPageAdapter.valueOfPage(PeerToPeerReviewState.class, pageName);
   }

   public static List<PeerToPeerReviewState> values() {
      return WorkPageAdapter.pages(PeerToPeerReviewState.class);
   }

};