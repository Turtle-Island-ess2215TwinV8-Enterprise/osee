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
package org.eclipse.osee.ats.hyper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Vector;
import java.util.logging.Level;
import org.eclipse.draw2d.ChopboxAnchor;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.ConnectionEndpointLocator;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FigureCanvas;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.LightweightSystem;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.MouseListener;
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.draw2d.ToolbarLayout;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osee.ats.AtsImage;
import org.eclipse.osee.ats.AtsOpenOption;
import org.eclipse.osee.ats.artifact.StateMachineArtifact;
import org.eclipse.osee.ats.editor.SMAEditor;
import org.eclipse.osee.ats.internal.AtsPlugin;
import org.eclipse.osee.ats.util.AtsUtil;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.logging.OseeLevel;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.ui.plugin.PluginUiImage;
import org.eclipse.osee.framework.ui.skynet.OseeContributionItem;
import org.eclipse.osee.framework.ui.skynet.artifact.editor.ArtifactEditor;
import org.eclipse.osee.framework.ui.swt.ImageManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

public class HyperView extends ViewPart implements IPartListener {

   private static final boolean debugOn = false;
   public static String VIEW_ID = "org.eclipse.osee.ats.hyper.HyperView";
   private LightweightSystem lws;
   private HyperViewItem centerSearchItem;
   private NodeFigure centerFigure;
   protected Composite composite;
   private Figure container; // main container Figure to hold others
   private XYLayout contentsLayout;
   private final Vector<PolylineConnection> connectors = new Vector<PolylineConnection>(); // PolylineConnection
   private final Vector<HyperViewItem> backList = new Vector<HyperViewItem>();
   // List of SearchItems visited
   private int backListIndex = 0; // Index we are on in backList
   private HyperViewItem homeSearchItem = null;
   // private static Canvas canvas;
   private FigureCanvas canvas;
   private final Cursor hCursor = null;
   private Menu popupMenu;
   private Menu connectPopupMenu;
   /* Full circle is (2*Math.PI) */
   private static final double quadrant = 2 * Math.PI / 4;
   /* Each quadrant is 90 degrees */
   private static final double oneEighthCircle = quadrant / 2; /* Start 45 degrees off 0 */
   private static final double bottomQuadrantStart = oneEighthCircle;
   private static final double topQuadrantStart = oneEighthCircle + 2 * quadrant;
   private static final double leftQuadrantStart = oneEighthCircle + quadrant;
   private static final double rightQuadrantStart = oneEighthCircle + 3 * quadrant;
   // private ChopboxAnchor sourceAnchor = null;
   private boolean dragCenter = false;
   protected static Color nodeColor = new Color(null, 255, 255, 206);
   protected static Color centerColor = new Color(null, 106, 219, 255);
   protected static final Color cyanColor = ColorConstants.cyan;
   protected static final Color blackColor = ColorConstants.black;
   protected static final Color whiteColor = ColorConstants.white;
   private ScrollBar vsb;
   private ScrollBar hsb;
   private Action titleAction, backAction, forwardAction;
   private Zoom zoom;
   protected Zoom defaultZoom = new Zoom();
   private int verticalSelection = 40;
   protected ArrayList<String> onlyShowRelations = new ArrayList<String>();

   public static class Zoom {

      int pcRadius = 50;
      int pcRadiusFactor = 5;
      int uuRadius = 60;
      int uuRadiusFactor = 20;
      int uuRadiusSeparation = 13; // left/right
      int pcRadiusSeparation = 13; // top/bottom
      int xSeparation = 27;
      int xSeparationFactor = 20;
      int ySeparation = 5;
      Point dragCenter = null;

      @Override
      public String toString() {
         StringBuilder builder = new StringBuilder();
         builder.append("xR:" + pcRadius);
         builder.append("yR:" + uuRadius);
         builder.append(" xRF:" + pcRadiusFactor);
         builder.append(" yRF:" + uuRadiusFactor);
         builder.append(" pcRS: " + pcRadiusSeparation);
         builder.append(" uuRS: " + uuRadiusSeparation);
         builder.append(" xS: " + xSeparation);
         builder.append(" yS: " + ySeparation);
         return builder.toString();
      }
   }

   enum RelationEnum {
      TOP, BOTTOM, BOTTOM2, LEFT, RIGHT
   };

   public boolean provideBackForwardActions() {
      return true;
   }

   public static class NodeFigure extends Figure {

      HyperViewItem hvi = null;

      public HyperViewItem getSearchItem() {
         return hvi;
      }

      public NodeFigure(Label name, Label infoLabel, HyperViewItem hvi, boolean center) {
         ToolbarLayout layout = new ToolbarLayout();
         layout.setVertical(false);
         setLayoutManager(layout);
         if (hvi.isCurrent()) {
            setBorder(new LineBorder(cyanColor, 3));
         } else {
            setBorder(new LineBorder(blackColor, 1));
         }
         if (center) {
            setBackgroundColor(centerColor);
         } else {
            setBackgroundColor(nodeColor);
         }
         setOpaque(true);
         this.hvi = hvi;
         if (name != null) {
            add(name);
         }
         if (infoLabel != null) {
            add(infoLabel);
         }
      }

      public HyperViewItem getHvi() {
         return hvi;
      }

   }

   public HyperView() {
      zoom = defaultZoom;
   }

   public void create(HyperViewItem hvi) {
      clear();
      gridLoad(hvi);
   }

   private void gridLoad(HyperViewItem hvi) {
      if (HyperView.debugOn) {
         System.out.println("gridLoad");
      }
      /*
       * If grid already loaded and it was a collection, remove old home and all links associated
       * with it.
       */
      if (homeSearchItem != null) {
         for (HyperViewItem hyperItem : homeSearchItem.getBottom()) {
            hyperItem.removeTop(homeSearchItem);
         }
      }
      homeSearchItem = hvi;
      centerSearchItem = hvi;
      backListAddSearchItem(homeSearchItem);
      jumpTo(homeSearchItem);
   }

   /**
    * This is a callback that will allow us to create the viewer and initialize it.
    */
   @Override
   public void createPartControl(Composite parent) {
      if (HyperView.debugOn) {
         System.out.println("createPartControl");
      }
      OseeContributionItem.addTo(this, true);

      canvas = new FigureCanvas(parent);
      canvas.setScrollBarVisibility(FigureCanvas.ALWAYS);
      vsb = canvas.getVerticalBar();
      vsb.setIncrement(1);
      vsb.setSelection(verticalSelection);
      vsb.addListener(SWT.Selection, new Listener() {

         public void handleEvent(Event event) {
            refresh();
            // System.out.println("vScroll " + event);
            // int v = canvas.getVerticalBar().getSelection();
            // System.out.println("v *" + v + "*");
         }
      });
      hsb = canvas.getHorizontalBar();
      hsb.setIncrement(1);
      hsb.setSelection(40);
      hsb.addListener(SWT.Selection, new Listener() {

         public void handleEvent(Event event) {
            refresh();
            // System.out.println("hScroll " + event);
            // int h = canvas.getVerticalBar().getSelection();
            // System.out.println("h *" + h + "*");
         }
      });
      // canvas = new Canvas(top, SWT.NONE);
      composite = parent;
      canvas.setBackground(composite.getDisplay().getSystemColor(SWT.COLOR_WHITE));
      lws = new LightweightSystem(canvas);
      container = new Figure();
      setContainerMouseListener(container);
      setMouseListener(container);
      contentsLayout = new XYLayout();
      container.setLayoutManager(contentsLayout);
      lws.setContents(container);
      // canvas.addPaintListener(new PaintListener() {
      //
      // public void paintControl(PaintEvent e) {
      // if (centerSearchItem == null || centerSearchItem.getArtifact() == null
      // || centerSearchItem.getArtifact().isDeleted())
      // return;
      // // refresh();
      // }
      // });
      canvas.layout();
      composite.layout();
      createActions();

   }

   private void printBackList(String inStr) {
      final boolean debug = false;
      if (debug) {
         StringBuilder builder = new StringBuilder();
         builder.append(inStr + "backList : [");
         for (int i = 0; i < backList.size(); i++) {
            builder.append(i + " ");
         }
         builder.append("] index: " + backListIndex);
         System.err.println(builder.toString());
      }
   }

   private void backListClearToIndex() {
      printBackList("pre backListClearToIndex");
      // Remove any extra items after where we are in backList
      for (int i = backListIndex + 1; i < backList.size(); i++) {
         backList.remove(i);
      }
      printBackList("post backListClearToIndex");
   }

   private void backListAddSearchItem(HyperViewItem hvi) {
      // If we are adding, then any items past backListIndex are
      // invalid and should be removed
      printBackList("pre backListAddSearchItem");
      backListClearToIndex();
      if (backList.size() > 0) {
         if (backList.get(backList.size() - 1).equals(hvi)) {
            // System.out.println("same as last; skip");
            return;
         }
      }
      backList.add(hvi);
      backListIndex = backList.size() - 1;
      printBackList("post backListAddSearchItem");
   }

   // private void backListClear() {
   // backList.clear();
   // backListIndex = 0;
   // }

   public void refresh() {
      gridDrawCenterItem();
   }

   protected void clear() {
      if (HyperView.debugOn) {
         System.out.println("gridClear");
      }
      if (connectors != null) {
         for (int i = 0; i < connectors.size(); i++) {
            connectors.get(i).erase();
         }
         connectors.clear();
      }
      if (container != null) {
         container.removeAll();
      }
   }

   private void gridDrawCenterItem() {
      clear();
      Rectangle grid = container.getClientArea();
      // System.out.println("grid w: " + grid.width + " h:" + grid.height);
      gridDraw(centerSearchItem, new Point(grid.width / 2, grid.height / 2));
   }

   private void gridDraw(HyperViewItem hvi, Point center) {
      if (HyperView.debugOn) {
         System.out.println("zoom *" + zoom + "*");
      }
      // Draw center node
      if (hvi != null) {
         if (HyperView.debugOn) {
            System.out.println("center x: " + center.x + " y:" + center.y);
         }
         if (HyperView.debugOn) {
            System.out.println("centerFigure *" + hvi.getTitle() + "*");
         }
         Point useCenter;
         if (zoom.dragCenter != null) {
            useCenter = zoom.dragCenter.getCopy();
         } else {
            useCenter = center.getCopy();
         }
         // Offset by scroll
         if (vsb != null) {
            useCenter.y -= (vsb.getSelection() - 40) * 10;
         }
         if (hsb != null) {
            useCenter.x -= (hsb.getSelection() - 40) * 10;
         }
         // System.out.println("zoom.dragCenter *" + zoom.dragCenter + "*");
         // System.out.println("center *" + center + "*");
         // System.out.println("vsb.getSelection() *" + vsb.getSelection() +
         // "*");
         // System.out.println("hsb.getSelection() *" + hsb.getSelection() +
         // "*");
         Point nwPoint;
         Point cPoint;
         centerFigure = createFigure(centerSearchItem, true);
         Dimension dim = centerFigure.getPreferredSize();
         if (HyperView.debugOn) {
            System.out.println("dim center w: " + dim.width + " h:" + dim.height);
         }
         nwPoint = new Point((useCenter.x - dim.width / 2), (useCenter.y - dim.height / 2));
         cPoint = new Point((nwPoint.x + dim.width / 2), (nwPoint.y + dim.height / 2));
         if (HyperView.debugOn) {
            System.out.println("nwPoint *" + nwPoint + "*");
         }
         if (HyperView.debugOn) {
            System.out.println("cPoint *" + cPoint + "*");
         }
         contentsLayout.setConstraint(centerFigure, new Rectangle(nwPoint.x, nwPoint.y, -1, -1));
         // centerFigure.repaint();
         ChopboxAnchor sourceAnchor = new ChopboxAnchor(centerFigure);
         setMouseListener(centerFigure);
         // debug only
         // drawQuadrant(topQuadrantStart, quadrant);
         // drawQuadrant(bottomQuadrantStart, quadrant);
         // drawQuadrant(leftQuadrantStart, quadrant);
         // drawQuadrant(rightQuadrantStart, quadrant);
         /*
          * Bottomren draw from right to left Tops draw from left to right Left draws from low to
          * high Usedby draws from high to low
          */
         // Draw other nodes
         drawNodes(RelationEnum.TOP, hvi.getTop(), cPoint, sourceAnchor);
         drawNodes(RelationEnum.BOTTOM, hvi.getBottom(), cPoint, sourceAnchor);
         drawNodes(RelationEnum.LEFT, hvi.getLeft(), cPoint, sourceAnchor);
         drawNodes(RelationEnum.RIGHT, hvi.getRight(), cPoint, sourceAnchor);
      }
      container.repaint();
      canvas.layout();
      composite.layout();
   }

   /**
    * Draw figures around center offsetPoint #param relType - relation
    * 
    * @param hvis - HyperViewItems to show.
    * @param offsetPoint - x,y to offset figures
    */
   private void drawNodes(RelationEnum relType, ArrayList<HyperViewItem> hvis, Point offsetPoint, ChopboxAnchor sourceAnchor) {
      if (HyperView.debugOn) {
         System.out.println("drawNodes");
      }
      if (hvis.size() == 0) {
         return;
      }
      final boolean isRight = relType == RelationEnum.RIGHT;
      final boolean isLeft = relType == RelationEnum.LEFT;
      final boolean isBottom = relType == RelationEnum.BOTTOM;
      final boolean isTop = relType == RelationEnum.TOP;
      if (HyperView.debugOn) {
         System.out.println("offsetPoint *" + offsetPoint + "*");
      }
      double startDegree = 0;
      if (isRight) {
         startDegree = HyperView.rightQuadrantStart;
      } else if (isLeft) {
         startDegree = HyperView.leftQuadrantStart;
      } else if (isTop) {
         startDegree = HyperView.topQuadrantStart;
      } else if (isBottom) {
         startDegree = HyperView.bottomQuadrantStart;
      }
      double degree = 0;
      Point cPoint = new Point(); // center of figure
      Point nwPoint = new Point();
      ArrayList<HyperViewItem> orderedHvis = new ArrayList<HyperViewItem>();

      // if bottomren, reverse order of figures
      if (isBottom) {
         for (int i = hvis.size() - 1; i >= 0; i--) {
            HyperViewItem hvi = hvis.get(i);
            if (hvi.isShow()) {
               orderedHvis.add(hvis.get(i));
            }
         }
      } else {
         for (HyperViewItem hvi : hvis) {
            if (hvi.isShow()) {
               orderedHvis.add(hvi);
            }
         }
      }
      final double degreeDiff = quadrant / (orderedHvis.size() + 1);
      if (HyperView.debugOn) {
         System.out.println("quadrant *" + quadrant + "*");
      }
      if (HyperView.debugOn) {
         System.out.println("orderedHvis size *" + orderedHvis.size() + "*");
      }
      if (HyperView.debugOn) {
         System.out.println("degreeDiff *" + degreeDiff + "*");
      }

      // what is sent into constraint as the nw corner
      int myRadius;
      if (isBottom || isTop) {
         myRadius = zoom.pcRadius;
      } else {
         myRadius = zoom.uuRadius;
      }
      for (int i = 0; i < orderedHvis.size(); i++) {
         HyperViewItem hvi = orderedHvis.get(i);
         if (HyperView.debugOn) {
            System.out.println("\n");
         }
         if (HyperView.debugOn) {
            System.out.println("HIV " + hvi.getShortTitle());
         }
         degree = startDegree + degreeDiff + degreeDiff * i;
         if (HyperView.debugOn) {
            System.out.println("startDegree *" + startDegree + "*");
         }
         if (HyperView.debugOn) {
            System.out.println("degree *" + degree + "*");
         }
         NodeFigure figure = createFigure(hvi, false);
         cPoint.x = offsetPoint.x + (int) Math.round((myRadius * Math.cos(degree)));
         cPoint.y = offsetPoint.y + (int) Math.round((myRadius * Math.sin(degree)));
         if (HyperView.debugOn) {
            drawX(offsetPoint.x, offsetPoint.y - 5, "o");
         }
         if (HyperView.debugOn) {
            drawX(cPoint.x, cPoint.y - 5, "c");
         }
         Dimension dim = figure.getPreferredSize();
         if (HyperView.debugOn) {
            System.out.println("dim " + relType + " w: " + dim.width + " h:" + dim.height);
         }
         if (isLeft) {
            nwPoint.x = cPoint.x - dim.width;
            nwPoint.y = cPoint.y - dim.height / 2;
         } else if (isRight) {
            nwPoint.x = cPoint.x;
            nwPoint.y = cPoint.y - dim.height / 2;
         } else if (isBottom) {
            nwPoint.x = cPoint.x - dim.width / 2;
            nwPoint.y = cPoint.y;
         } else if (isTop) {
            nwPoint.x = cPoint.x - dim.width / 2;
            nwPoint.y = cPoint.y - dim.height;
         }
         if (orderedHvis.size() > 1 && (isTop || isBottom)) {
            if (i < orderedHvis.size() / 2) {
               myRadius += zoom.pcRadiusSeparation;
               if (isBottom) {
                  nwPoint.x += zoom.xSeparation;
               }
               if (isTop) {
                  nwPoint.x -= zoom.xSeparation;
               }
            } else if (i > orderedHvis.size() / 2) {
               myRadius -= zoom.pcRadiusSeparation;
               if (isBottom) {
                  nwPoint.x -= zoom.xSeparation;
               }
               if (isTop) {
                  nwPoint.x += zoom.xSeparation;
               }
            }
         }
         if (orderedHvis.size() > 1 && (isLeft || isRight)) {
            // int half = orderedFigures.size() / 2;
            if (i < orderedHvis.size() / 2) {
               myRadius += zoom.uuRadiusSeparation;
            } else {
               myRadius -= zoom.uuRadiusSeparation;
            }
         }
         contentsLayout.setConstraint(figure, new Rectangle(nwPoint.x, nwPoint.y, -1, -1));
         setMouseListener(figure);
         /* Draw line to left */
         ChopboxAnchor targetAnchor = new ChopboxAnchor(figure);
         drawLine(sourceAnchor, targetAnchor, figure.getHvi());
         // .getRelationToolTip(),
         // ((NodeFigure) figure).getHvi().getRelationLabel(), ((NodeFigure)
         // figure).getHvi().getRelationDirty());

         if (HyperView.debugOn) {
            System.out.println("cPoint *" + cPoint + "*");
         }
         if (HyperView.debugOn) {
            System.out.println("nwPoint *" + nwPoint + "*");
         }
         if (HyperView.debugOn) {
            System.out.println("offsetPoint *" + offsetPoint + "*");
         }
         ChopboxAnchor thisAnchor = new ChopboxAnchor(figure);
         if (hvi.getTop().size() > 0) {
            drawNodes(RelationEnum.TOP, hvi.getTop(), cPoint, thisAnchor);
         }
         if (hvi.getBottom().size() > 0) {
            drawNodes(RelationEnum.BOTTOM, hvi.getBottom(), new Point((nwPoint.x + dim.width / 2),
                  (nwPoint.y + dim.height / 2)), thisAnchor);
         }
         if (hvi.getLeft().size() > 0) {
            drawNodes(RelationEnum.LEFT, hvi.getLeft(), cPoint, thisAnchor);
         }
         if (hvi.getRight().size() > 0) {
            drawNodes(RelationEnum.RIGHT, hvi.getRight(), cPoint, thisAnchor);
         }
      }
   }

   /*
    * Used for debug only drawQuadrant(leftQuadrantStart,quadrant);
    */
   // @SuppressWarnings("unused")
   // private void drawQuadrant(double start, double degrees) {
   // int cx = 100, cy = 100, r = 100;
   // int x, y;
   // double a;
   // for (a = start; a < start + degrees; a += 0.01) {
   // x = (int) Math.round(r * Math.cos(a)) + cx;
   // y = (int) Math.round(r * Math.sin(a)) + cy;
   // drawX(x, y, "x");
   // }
   // double degreeDiff = degrees / 4;
   // for (int t = 0; t < 3; t += 1) {
   // x = (int) Math.round(r * Math.cos(start + degreeDiff + (t * degreeDiff))) + cx;
   // y = (int) Math.round(r * Math.sin(start + degreeDiff + (t * degreeDiff))) + cy;
   // drawX(x, y + 20, "x");
   // }
   // }
   private void drawX(int x, int y, String str) {
      Label label = new Label(str, null);
      Figure figure = new NodeFigure(label, null, new HyperViewItem(""), false);
      container.add(figure);
      contentsLayout.setConstraint(figure, new Rectangle(x, y, -1, -1));
   }

   private void drawLine(ChopboxAnchor source, ChopboxAnchor target, HyperViewItem hvi) {
      PolylineConnection c = new PolylineConnection();
      c.setLineWidth(2);
      if (hvi.isRelationDirty()) {
         c.setForegroundColor(Display.getCurrent().getSystemColor(SWT.COLOR_BLUE));
      }
      c.setSourceAnchor(source);
      c.setTargetAnchor(target);
      setConnectionMouseListener(c, hvi);
      // Add tooltip
      if (hvi.getRelationToolTip() != null && !hvi.getRelationToolTip().equals("")) {
         c.setToolTip(new Label(hvi.getRelationToolTip()));
         // Add label
      }

      ConnectionEndpointLocator targetEndpointLocator = new ConnectionEndpointLocator(c, true);
      targetEndpointLocator.setVDistance(1);
      Label targetMultiplicityLabel = new Label(hvi.getRelationLabel());
      c.add(targetMultiplicityLabel, targetEndpointLocator);

      connectors.add(c);
      container.add(c);
   }

   protected void display() {
   }

   private void createConnectionMenuBar(final HyperViewItem hvi) {
      if (AtsUtil.isAtsAdmin()) {
         if (connectPopupMenu == null || !connectPopupMenu.isVisible()) {
            // final PolylineConnection c = connection;
            connectPopupMenu = new Menu(canvas.getShell(), SWT.POP_UP);
            MenuItem editItem = new MenuItem(connectPopupMenu, SWT.CASCADE);
            editItem.setText("Delete Link");
            editItem.addSelectionListener(new SelectionAdapter() {

               @Override
               public void widgetSelected(SelectionEvent e) {
                  try {
                     if (MessageDialog.openQuestion(Display.getCurrent().getActiveShell(), "Delete Link",
                           "Delete Link\n\n" + hvi.getLink().toString() + "\n\nAre you sure?")) {
                        Artifact artA = hvi.getLink().getArtifactA();
                        hvi.getLink().delete(true);
                        artA.persist();
                        connectPopupMenu.dispose();
                        connectPopupMenu = null;
                        display();
                     }
                  } catch (OseeCoreException ex) {
                     OseeLog.log(AtsPlugin.class, Level.SEVERE, ex);
                  }
               }
            });
            connectPopupMenu.setVisible(true);
         }
      }
   }

   private void setConnectionMouseListener(PolylineConnection c, final HyperViewItem hvi) {
      MouseListener mouseListener = new MouseListener() {

         public void mouseDoubleClicked(MouseEvent e) {
            if (HyperView.debugOn) {
               System.out.println("DoubleClick");
            }
         }

         public void mousePressed(MouseEvent e) {
            if (HyperView.debugOn) {
               System.out.println("mousePressed");
            }
         }

         public void mouseReleased(MouseEvent e) {
            if (HyperView.debugOn) {
               System.out.println("mouseReleased");
            }
            if (e.button == 3) {
               if (e.getSource() instanceof PolylineConnection) {
                  // PolylineConnection c = (PolylineConnection) e.getSource();
                  createConnectionMenuBar(hvi);
               }
               return;
            }
         }
      };
      c.addMouseListener(mouseListener);
   }

   private NodeFigure createFigure(HyperViewItem hvi, boolean center) {
      Label infoLabel = null;
      String title = hvi.getShortTitle();
      if (!titleAction.isChecked()) {
         if (title.length() > 30) {
            title = title.substring(0, 30);
         }
      }
      Label nameLabel = null;
      try {
         nameLabel = new Label(title, hvi.getImage());
      } catch (OseeCoreException ex) {
         OseeLog.log(AtsPlugin.class, OseeLevel.SEVERE, ex);
         nameLabel = new Label(title, null);
      }
      Image markImage = hvi.getMarkImage();
      if (markImage != null) {
         infoLabel = new Label(null, markImage);
      }

      NodeFigure figure = new NodeFigure(nameLabel, infoLabel, hvi, center);
      // System.out.println(title + " " + figure.hashCode());
      container.add(figure);
      figure.setToolTip(new Label(hvi.getToolTip()));
      return figure;
   }

   private void setContainerMouseListener(Figure figure) {
      MouseListener mouseListener = new MouseListener() {

         public void mouseDoubleClicked(MouseEvent e) {
         }

         public void mousePressed(MouseEvent e) {
         }

         public void mouseReleased(MouseEvent e) {
            if (e.button == 1) {
               if (dragCenter) {
                  if (zoom.dragCenter == null) {
                     zoom.dragCenter = new Point();
                  }
                  zoom.dragCenter.x = e.x;
                  zoom.dragCenter.y = e.y;
                  refresh();
                  dragCenter = false;
                  if (hCursor != null) {
                     container.setCursor(null);
                     centerFigure.setCursor(null);
                     hCursor.dispose();
                  }
               }
            }
         }
      };
      figure.addMouseListener(mouseListener);
   }

   private void setMouseListener(Figure figure) {
      MouseListener mouseListener = new MouseListener() {

         public void mouseDoubleClicked(MouseEvent e) {
            if (e.button == 1 && e.getSource() instanceof NodeFigure) {
               NodeFigure nf = (NodeFigure) e.getSource();
               HyperViewItem si = nf.getSearchItem();
               try {
                  handleItemDoubleClick(si);
               } catch (OseeCoreException ex) {
                  OseeLog.log(AtsPlugin.class, OseeLevel.SEVERE_POPUP, ex);
               }
            }
         }

         public void mousePressed(MouseEvent e) {
         }

         public void mouseReleased(MouseEvent e) {
            // System.out.println("mouseReleased");
            if (hCursor != null) {
               container.setCursor(null);
               centerFigure.setCursor(null);
               hCursor.dispose();
            }
            if (e.getSource() instanceof NodeFigure) {
               NodeFigure nf = (NodeFigure) e.getSource();
               HyperViewItem si = nf.getSearchItem();
               if (HyperView.debugOn) {
                  System.out.println("Click: " + si.getTitle());
               }
               if (e.button == 3) {
                  createMenuBar(si);
               }
            }
         }
      };
      figure.addMouseListener(mouseListener);
   }

   public void handleItemDoubleClick(HyperViewItem hvi) throws OseeCoreException {
      backListAddSearchItem(hvi);
      jumpTo(hvi);
   }

   public static void openActionEditor(Artifact artifact) {
      if (artifact instanceof StateMachineArtifact) {
         AtsUtil.openATSAction(artifact, AtsOpenOption.OpenOneOrPopupSelect);
      }
   }

   private void createMenuBar(HyperViewItem hvi) {
      if (popupMenu == null || !popupMenu.isVisible()) {
         final HyperViewItem fHvi = hvi;
         popupMenu = new Menu(canvas.getShell(), SWT.POP_UP);

         if (AtsUtil.isAtsAdmin()) {
            MenuItem editItem = new MenuItem(popupMenu, SWT.CASCADE);
            editItem.setText("Open in Artifact Editor");
            editItem.addSelectionListener(new SelectionAdapter() {

               @Override
               public void widgetSelected(SelectionEvent e) {
                  Artifact a = null;
                  if (fHvi instanceof ArtifactHyperItem) {
                     a = ((ArtifactHyperItem) fHvi).getArtifact();
                  } else if (fHvi instanceof ActionHyperItem) {
                     a = ((ActionHyperItem) fHvi).getArtifact();
                  }
                  if (a != null) {
                     ArtifactEditor.editArtifact(a);
                  }
                  popupMenu.dispose();
                  popupMenu = null;
               }
            });
            MenuItem deleteItem = new MenuItem(popupMenu, SWT.CASCADE);
            deleteItem.setText("Delete Artifact");
            deleteItem.addSelectionListener(new SelectionAdapter() {

               @Override
               public void widgetSelected(SelectionEvent e) {
                  Artifact art = null;
                  if (fHvi instanceof ArtifactHyperItem) {
                     art = ((ArtifactHyperItem) fHvi).getArtifact();
                  } else {
                     art = ((ActionHyperItem) fHvi).getArtifact();
                  }
                  String dialogTitle = "Confirm Artifact Deletion?";
                  StringBuilder dialogText = new StringBuilder();
                  dialogText.append("\"" + art.getName() + "\"\nguid: " + art.getGuid());
                  dialogText.append("\n\n Are you sure you want to delete this artifact and its default-hierarchy children?");
                  if (MessageDialog.openQuestion(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
                        dialogTitle, dialogText.toString())) {
                     if (art instanceof StateMachineArtifact) {
                        SMAEditor.close(Collections.singleton((StateMachineArtifact) art), false);
                     }
                     try {
                        art.deleteAndPersist();
                     } catch (Exception ex) {
                        OseeLog.log(AtsPlugin.class, OseeLevel.SEVERE_POPUP, ex);
                     }
                  }
                  popupMenu.dispose();
                  popupMenu = null;
               }
            });
         }

         MenuItem previewItem = new MenuItem(popupMenu, SWT.CASCADE);
         previewItem.setText("Open in ATS");
         previewItem.setEnabled(fHvi instanceof ArtifactHyperItem && ((ArtifactHyperItem) fHvi).getArtifact() instanceof StateMachineArtifact || fHvi instanceof ActionHyperItem && ((ActionHyperItem) fHvi).getArtifact() instanceof StateMachineArtifact);
         previewItem.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
               Artifact a = null;
               if (fHvi instanceof ArtifactHyperItem) {
                  a = ((ArtifactHyperItem) fHvi).getArtifact();
               } else if (fHvi instanceof ActionHyperItem) {
                  a = ((ActionHyperItem) fHvi).getArtifact();
               }
               if (a != null) {
                  openActionEditor(a);
               }
            }
         });
         popupMenu.setVisible(true);
      }
   }

   public void jumpTo(HyperViewItem si) {
      if (homeSearchItem == null) {
         return;
      }
      centerSearchItem = si;
      refresh();
   }

   protected void center() {
      zoom = defaultZoom;
      vsb.setSelection(verticalSelection);
      hsb.setSelection(40);
      refresh();
   }

   protected void createActions() {

      // Reset Action
      Action centerAction = new Action() {

         @Override
         public void run() {
            center();
         }
      };
      centerAction.setText("Center");
      centerAction.setToolTipText("Center");
      centerAction.setImageDescriptor(ImageManager.getImageDescriptor(AtsImage.CENTER));

      if (provideBackForwardActions()) {
         // Back Action
         backAction = new Action() {

            @Override
            public void run() {
               printBackList("pre backSelected");
               if (backList.size() == 0) {
                  return;
               }
               if (backListIndex > 0) {
                  backListIndex--;
               }
               jumpTo(backList.get(backListIndex));
               printBackList("post backSelected");
            }
         };

         backAction.setText("Back");
         backAction.setToolTipText("Back");
         backAction.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(
               ISharedImages.IMG_TOOL_BACK));
         // Forward Action
         forwardAction = new Action() {

            @Override
            public void run() {
               printBackList("pre forwardSelected");
               if (backList.size() - 1 > backListIndex) {
                  backListIndex++;
                  jumpTo(backList.get(backListIndex));
               }
               printBackList("post forwardSelected");
            }
         };
         forwardAction.setText("Forward");
         forwardAction.setToolTipText("Forward");
         forwardAction.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(
               ISharedImages.IMG_TOOL_FORWARD));
      }
      // // Home
      // Action homeAction = new Action() {
      //
      // public void run() {
      // if (homeSearchItem == null) {
      // return;
      // }
      // vsb.setSelection(verticalSelection);
      // hsb.setSelection(40);
      // backListAddSearchItem(homeSearchItem);
      // jumpTo(homeSearchItem);
      // }
      // };
      // homeAction.setText("Home");
      // homeAction.setToolTipText("Home");
      // homeAction.setImageDescriptor(AtsPlugin.getImageDescriptor("home.gif"));
      // Zoom in
      Action zoomInAction = new Action() {

         @Override
         public void run() {
            if (homeSearchItem == null) {
               return;
            }
            if (HyperView.debugOn) {
               System.out.println("zoomInAction");
            }
            zoom.pcRadius += zoom.pcRadiusFactor;
            zoom.uuRadius += zoom.uuRadiusFactor;
            zoom.xSeparation += zoom.xSeparationFactor;
            refresh();

         }
      };
      zoomInAction.setText("Zoom In");
      zoomInAction.setToolTipText("Zoom In");
      zoomInAction.setImageDescriptor(ImageManager.getImageDescriptor(AtsImage.ZOOM_IN));
      // Zoom Out
      Action zoomOutAction = new Action() {

         @Override
         public void run() {
            if (homeSearchItem == null) {
               return;
            }
            if (HyperView.debugOn) {
               System.out.println("zoomOutAction");
            }
            if (zoom.pcRadius >= zoom.pcRadiusFactor) {
               zoom.pcRadius -= zoom.pcRadiusFactor;
            }
            if (zoom.uuRadius >= zoom.uuRadiusFactor) {
               zoom.uuRadius -= zoom.uuRadiusFactor;
            }
            if (zoom.xSeparation >= zoom.xSeparationFactor) {
               zoom.xSeparation -= zoom.xSeparationFactor;
            }
            refresh();
         }
      };
      zoomOutAction.setText("Zoom Out");
      zoomOutAction.setToolTipText("Zoom Out");
      zoomOutAction.setImageDescriptor(ImageManager.getImageDescriptor(AtsImage.ZOOM_OUT));

      titleAction = new Action("Expand Titles", IAction.AS_CHECK_BOX) {

         @Override
         public void run() {
            if (HyperView.debugOn) {
               System.out.println("expandTitles");
            }
            refresh();
         }
      };
      titleAction.setToolTipText("Expand Titles");

      Action refreshAction = new Action("Refresh") {

         @Override
         public void run() {
            if (homeSearchItem == null) {
               MessageDialog.openInformation(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
                     "Refresh Error", "Viewer not loaded, nothing to refresh.");
               return;
            }
            handleRefreshButton();
         }
      };
      refreshAction.setToolTipText("Refresh");
      ImageManager.getImageDescriptor(PluginUiImage.REFRESH);
      refreshAction.setImageDescriptor(ImageManager.getImageDescriptor(PluginUiImage.REFRESH));

      IActionBars bars = getViewSite().getActionBars();
      IMenuManager mm = bars.getMenuManager();
      mm.add(new Separator());
      mm.add(titleAction);

      IToolBarManager tbm = bars.getToolBarManager();
      tbm.add(new Separator());
      // tbm.add(homeAction);
      tbm.add(centerAction);
      tbm.add(zoomInAction);
      tbm.add(zoomOutAction);
      if (provideBackForwardActions()) {
         tbm.add(backAction);
         tbm.add(forwardAction);
      }
      tbm.add(refreshAction);

   }

   protected void handleRefreshButton() {
      refresh();
   }

   /**
    * Passing the focus request to the viewer's control.
    */
   @Override
   public void setFocus() {
      // viewer.getControl().setFocus();
   }

   public Color getCenterColor() {
      return HyperView.centerColor;
   }

   public static void setCenterColor(Color color) {
      centerColor = color;
   }

   public Color getNodeColor() {
      return HyperView.nodeColor;
   }

   public static void setNodeColor(Color color) {
      nodeColor = color;
   }

   public int getVerticalSelection() {
      return verticalSelection;
   }

   public void setVerticalSelection(int verticalSelection) {
      this.verticalSelection = verticalSelection;
      if (vsb != null) {
         vsb.setSelection(verticalSelection);
      }
   }

   @Override
   public void dispose() {
   }

   public void partActivated(IWorkbenchPart part) {
   }

   public void partBroughtToTop(IWorkbenchPart part) {
   }

   public void partClosed(IWorkbenchPart part) {
      if (part.equals(this)) {
         dispose();
      }
   }

   public void partDeactivated(IWorkbenchPart part) {
   }

   public void partOpened(IWorkbenchPart part) {
   }

   public Figure getContainer() {
      return container;
   }
}