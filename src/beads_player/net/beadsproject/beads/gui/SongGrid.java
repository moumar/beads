package net.beadsproject.beads.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import net.beadsproject.beads.play.Player;
import net.beadsproject.beads.play.SongGroup;
import net.beadsproject.beads.play.SongPart;

public class SongGrid extends BeadsPanel {

	private static final long serialVersionUID = 1L;
	private JComponent grid;
	private JComponent groupPanel;
	private JComponent partPanel;
	private List<SongPart> parts;
	private List<SongGroup> groups;
	private Map<SongPart, BeadsWindow> partWindows;
	private int boxWidth = 20;
	private int partTextWidth = 50;
	private int groupTextHeight = 50;
	private Player player;
	private EnvironmentPanel environmentPanel;
	
	public SongGrid(Player p, EnvironmentPanel ep) {
		this.player = p;
		this.environmentPanel = ep;
		parts = new ArrayList<SongPart>();
		groups = new ArrayList<SongGroup>();
		partWindows = new Hashtable<SongPart, BeadsWindow>();
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		JPanel vPanel = new JPanel();
		vPanel.setLayout(new BoxLayout(vPanel, BoxLayout.Y_AXIS));
		partPanel = new JComponent() {
			public void paintComponent(Graphics g) {
				Graphics2D g2d = ((Graphics2D)g);
				g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g2d.setColor(Color.white);
				g2d.fillRect(0, 0, getWidth(), getHeight());
				g2d.setColor(Color.black);
				int i = 1;
				synchronized(parts) {
					for(SongPart sp : parts) {
						g2d.drawString(sp.getName(), 5, groupTextHeight + i++ * boxWidth - 4);
					}
				}
			}
		};
		partPanel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if(e.getY() > groupTextHeight) {
					SongPart sp = parts.get((e.getY() - groupTextHeight) / boxWidth);
					if(e.getClickCount() > 1) {
						if(partWindows.containsKey(sp)) {
							partWindows.get(sp).setVisible(true);
							partWindows.get(sp).toFront();
						} else {
							BeadsWindow bw = new BeadsWindow(sp.getName());
							bw.content.add(sp.getComponent());
							bw.pack();
							bw.setVisible(true);
							bw.setResizable(false);
							partWindows.put(sp, bw);
						}
					} else {
						if((e.getModifiers() & MouseEvent.CTRL_MASK) != 0) {
							//TODO connect this SongPart to the element in the environment panel
							//build a popup
							JPopupMenu m = environmentPanel.getChannelsPathwaysPopupMenu(sp);
							m.show(partPanel, e.getX(), e.getY());
						}
					}
				} else {
					if((e.getModifiers() & MouseEvent.CTRL_MASK) != 0) {
						JPopupMenu m = new JPopupMenu();
						//get list of possible song parts??
						JMenuItem newGroupItem = new JMenuItem("new group");
						newGroupItem.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent e) {
								addSongGroup();
							}
						});
						m.add(newGroupItem);
						m.show(partPanel, e.getX(), e.getY());
					}
				}
			}
		});
		add(partPanel);
		groupPanel = new JComponent() {
			public void paintComponent(Graphics g) {
				Graphics2D g2d = ((Graphics2D)g);
				g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g2d.setColor(Color.white);
				g2d.fillRect(0, 0, getWidth(), getHeight());
				int i = 1;
				for(SongGroup sg : groups) {
					g2d.rotate(-Math.PI / 2f);
					if(player.getCurrentGroup() == sg) {
						g2d.setColor(Color.gray);
						g2d.fillRect(-groupTextHeight, (i - 1) * boxWidth, groupTextHeight, boxWidth);
					}
					g2d.setColor(Color.black);
					g2d.drawString(sg.getName(), -groupTextHeight + 5, i++ * boxWidth - 4);
					g2d.rotate(Math.PI / 2f);
				}
			}
		};
		groupPanel.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				if((e.getModifiers() & MouseEvent.CTRL_MASK) != 0) {
					JPopupMenu jp = groupsPopupMenu();
					jp.show(groupPanel, e.getX(), e.getY());
				} else {
					SongGroup g = groups.get(e.getX() / boxWidth);
					if(getCurrentGroup() != g) {
						player.playGroup(g);
					} else {
						player.stop();
					}
					repaint();
				}
			}
		});
		vPanel.add(groupPanel);
		grid = new JComponent() {
			public void paintComponent(Graphics g) {
				((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g.setColor(Color.white);
				g.fillRect(0, 0, getWidth(), getHeight());
				g.setColor(Color.gray);
				int i = 0;
				synchronized(groups) {
					for(SongGroup sg : groups) {
						int j = 0;
						synchronized(parts) {
							for(SongPart sp : parts) {
								//draw a dot
								if(sg.contains(sp)) {
									g.fillRect(i * boxWidth, j * boxWidth, boxWidth, boxWidth);
								} else {
									g.drawRect(i * boxWidth, j * boxWidth, boxWidth, boxWidth);
								}
								j++;
							}
						}
						i++;
					}
				}
				g.drawLine(0, getHeight() - 1, getWidth(), getHeight() - 1);
			}
		};
		grid.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				SongGroup g = groups.get(e.getX() / boxWidth);
				SongPart p = parts.get(e.getY() / boxWidth);
				if(g != null && p != null) {
					if(g.contains(p)) {
						g.remove(p);
					} else {
						g.add(p);
					}
					//this is super-inefficient, but self-evidently dependable
					player.notifyCurrentGroupUpdated();
				}
				grid.repaint();
			}
			
		});
		vPanel.add(grid);
		add(vPanel);
	}
	
	private JPopupMenu groupsPopupMenu() {
		JPopupMenu jp = new JPopupMenu();
		JMenuItem newGroupItem = new JMenuItem("New Group...");
		newGroupItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				MicroPopup mp = new MicroPopup((Frame)getTopLevelAncestor(), "group name");
				String name = mp.getText();
				addSongGroup(new SongGroup(name));
				repaint();
			}
		});
		jp.add(newGroupItem);
		return jp;
	}
	
	public void addSongPart(SongPart sp) {
		parts.add(sp);
		environmentPanel.getSelectedChannel().addInput(sp);
		environmentPanel.getSelectedPathway().add(sp);
		sp.setClock(environmentPanel.getSelectedClock());
		resize();
	}
	
	public void addSongGroups(int numGroups) {
		for(int i = 0; i < numGroups; i++) {
			addSongGroup();
		}
	}
	
	public void addSongGroup() {
		addSongGroup(new SongGroup("g" + (groups.size() + 1)));
	}
	
	public void addSongGroup(SongGroup sg) {
		groups.add(sg);
		resize();
	}
	
	public void resize() {
		//oh no, I want to kill myself when I see this code
		groupPanel.setMinimumSize(new Dimension(groups.size() * boxWidth + 1, groupTextHeight));
		grid.setMinimumSize(new Dimension(groups.size() * boxWidth + 1, parts.size() * boxWidth));
		partPanel.setMinimumSize(new Dimension(partTextWidth, groupTextHeight + parts.size() * boxWidth + 1));
		//this is so insane! just look at all of these resizes
		groupPanel.setPreferredSize(new Dimension(groups.size() * boxWidth + 1, groupTextHeight));
		grid.setPreferredSize(new Dimension(groups.size() * boxWidth + 1, parts.size() * boxWidth + 1));
		partPanel.setPreferredSize(new Dimension(partTextWidth, groupTextHeight + parts.size() * boxWidth + 1));
		//nope, still not done yet, this really is fucked up
		groupPanel.setMaximumSize(new Dimension(groups.size() * boxWidth + 1, groupTextHeight));
		grid.setMaximumSize(new Dimension(groups.size() * boxWidth + 1, parts.size() * boxWidth + 1));
		partPanel.setMaximumSize(new Dimension(partTextWidth, groupTextHeight + parts.size() * boxWidth + 1));
		//why o why? What was the point of life again? why am I making this program, something to do with music software, simple interfaces, I can't remember
		groupPanel.revalidate();
		grid.revalidate();
		partPanel.revalidate();
		//ugh! I like Swing when I'm not fucking around with resizing and redrawing!
		//ahh, back to something enjoyable.
	}
	
	public int getBoxWidth() {
		return boxWidth;
	}

	public void setBoxWidth(int boxWidth) {
		this.boxWidth = boxWidth;
	}
	
	public int getPartTextWidth() {
		return partTextWidth;
	}
	
	public void setPartTextWidth(int partTextWidth) {
		this.partTextWidth = partTextWidth;
	}
	
	public int getGroupTextHeight() {
		return groupTextHeight;
	}
	
	public void setGroupTextHeight(int groupTextHeight) {
		this.groupTextHeight = groupTextHeight;
	}
	
	public SongGroup getCurrentGroup() {
		return player.getCurrentGroup();
	}

	public void setCurrentGroup(SongGroup currentGroup) {
		player.setCurrentGroup(currentGroup);
	}

	public void setCurrentGroup(int i) {
		player.setCurrentGroup(groups.get(i));
	}

}
