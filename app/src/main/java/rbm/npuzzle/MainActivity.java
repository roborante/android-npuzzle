package rbm.npuzzle;

/////////////////////////////////////////////////////////////////////////////////////////

import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/////////////////////////////////////////////////////////////////////////////////////////

class Var
{
    static       int             [ ] puzzle    = new int             [ 0];
    static       int             [ ] positions = new int             [ 9];
    static final ImageButton     [ ] tiles     = new ImageButton     [16];
    static       ConstraintLayout[ ] boxes     = new ConstraintLayout[ 9];
    static final ArrayList <Integer> solution  = new ArrayList<     >(  );
    
    static int     side   = 3;
    static int     size   = 9;
    static long    thread =-1;
    static Thread  ida    = null ;
    static boolean info   = false;
    static boolean goal   = false;
    
    static long          lastClick = 0;
	static final Handler checkGoal = new Handler( );
	static final Handler checkIda  = new Handler( );
}

/////////////////////////////////////////////////////////////////////////////////////////

public class MainActivity extends AppCompatActivity
{
 
///////////////////////////////////////////////////////////////////////////////////////// c++
	
	static
    {
        System.loadLibrary("puzzle8" );
        System.loadLibrary("puzzle15");
    }
                                            private native int    heuristic8 (int[ ] permutation);
                                            private native int    heuristic15(int[ ] permutation);
    @SuppressWarnings("JniMissingFunction") private native int[ ] random8    (                  );
    @SuppressWarnings("JniMissingFunction") private native int[ ] random15   (                  );
    @SuppressWarnings("JniMissingFunction") private native int[ ] idaStar8   (int[ ] permutation);
    @SuppressWarnings("JniMissingFunction") private native int[ ] idaStar15  (int[ ] permutation, int bound);

/////////////////////////////////////////////////////////////////////////////////////////
	
	@Override
	public void onDestroy( )
    {
        super.onDestroy( );
        
        Var.checkGoal.removeCallbacks(beHappy   );
        Var.checkIda .removeCallbacks(idaRunning);
	}

/////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate( savedInstanceState );
        setContentView(R.layout.main_layout);

        if(Var.puzzle.length != Var.size)
        {
            Var.puzzle = random8( );
        	
        	for(int k=0;k<16;++k)
	        {
		        Var.tiles[k] = new ImageButton    (this );
		        Var.tiles[k].setBackgroundResource(getSticker(k));
	
	            if(k!=0)
	            {
		            final int move = k;
			        Var.tiles[k].setOnClickListener(new View.OnClickListener( )
		            {
		                @Override
		                public void onClick(View v)
		                {
		                    if(Var.thread == -1 && !Var.goal && okClick( ))
		                    {
		                        slide_mechanism(move,false);
		                    }
		                }
		            });
	            }
	        }
        }
        
        Var.goal = isGoal          ( );
                   show_what_needed( );
                   mode_button     ( );
                   ida_button      ( );
                   setBoard        ( );
                   beHappy.run     ( );
        
        if(Var.info) showInfo(false);
        
        if(Var.thread != -1)
        {
        	if(Var.solution.size( ) != 0)
	        {
	        	Var.thread = -1;
                
                find_solution(false,false);
	        }
        	else
	        {
	        	gears_spin(false);
	        	idaRunning.run( );
	        }
        }
        
        else if(Var.ida != null)
        {
        	idaRunning.run( );
        }

///////////////////////////////////////////////////////////////////////////////////////// action listeners

        findViewById(R.id.about).setOnClickListener(new View.OnClickListener( )
        {
            @Override
            public void onClick(View v)
            {
                showInfo(true);
            }
        });

/////////////////////////////////////////////////////////////////////////////////////////

        findViewById(R.id.mode).setOnClickListener(new View.OnClickListener( )
        {
            @Override
            public void onClick(View v)
            {
                if(okClick( ))
                {
                	stopIda( );
                	Var.size = Var.size == 9 ? 16 : 9;
	                Var.side = Var.side == 3 ?  4 : 3;
	                
	                Var.positions = new int             [Var.size];
	                Var.boxes     = new ConstraintLayout[Var.size];
	                
	                show_what_needed( );
					mode_button     ( );
	                new_game        (false);
                }
            }
        });

/////////////////////////////////////////////////////////////////////////////////////////

        findViewById(R.id.new_game).setOnClickListener(new View.OnClickListener( )
        {
            @Override
            public void onClick(View v)
            {
                if(okClick( ))
                {
                	stopIda ( );
                	new_game(true);
                }
            }
        });

/////////////////////////////////////////////////////////////////////////////////////////

        findViewById(R.id.idaStar).setOnClickListener(new View.OnClickListener( )
        {
            @Override
            public void onClick(View v)
            {
                if(okClick( ))
                {
	                if(Var.thread != -1)
	                {
	                    stopIda( );
	                }
	                
	                else if(Var.ida == null && !Var.goal)
	                {
	                    find_solution(true,true);
	                }
	                
	                ida_button( );
                }
            }
        });
    }

/////////////////////////////////////////////////////////////////////////////////////////
    
    @Override
	public void onBackPressed( )
    {
        final TextView info = findViewById(R.id.informations);
    	
    	if(Var.info && info.getVisibility( ) == View.VISIBLE)
		{
			Var.info = false;
			
			runOnUiThread(new Runnable( )
		    {
			    @Override
			    public void run( )
			    {
			        final Animation anim = AnimationUtils.loadAnimation(getApplicationContext( ),R.anim.slide_up_info);
	                
	                anim.setAnimationListener(new Animation.AnimationListener( )
	                {
	                    @Override public void onAnimationStart (Animation arg)
	                    {
	                    	info.setText("");
	                    }
	                    @Override public void onAnimationRepeat(Animation arg){ }
	                    @Override public void onAnimationEnd   (Animation arg)
	                    {
	                        info.setVisibility (View.GONE);
	                        info.clearAnimation(         );
	                    }
	                });
	                
	                info.startAnimation(anim);
			    }
		    });
		}
    	
    	else System.exit(0);
    }

///////////////////////////////////////////////////////////////////////////////////////// functions

    private void new_game(final boolean sameSize)
    {
        Var.checkGoal.removeCallbacks(beHappy);
    	
    	Var.puzzle = Var.size == 9 ? random8( ) : random15( );
		Var.goal   = isGoal( );
        
        if(sameSize) stop_slide ( );
                     ida_button ( );
                     setBoard   ( );
                     beHappy.run( );
    }

/////////////////////////////////////////////////////////////////////////////////////////
	
	private boolean okClick( )
	{
		final long time = SystemClock.elapsedRealtime( );
		
		if(time - Var.lastClick > 162)
		{
			Var.lastClick = time;
			
			return true;
		}
		
		return false;
	}
	
/////////////////////////////////////////////////////////////////////////////////////////

    private void setBoard( )
    {
        runOnUiThread(new Runnable( )
	    {
		    @Override
		    public void run( )
		    {
			    for(int k=0;k<Var.size;++k)
		        {
		            final int tile = Var.puzzle[k];
		        	
		        	Var.positions[tile] = k ;
        	        Var.boxes[k] = getPos(k);
        	
        	        ConstraintLayout box = (ConstraintLayout) Var.tiles[tile].getParent( );
		            
		            if (box != null) box.removeView(Var.tiles[tile]);
		            
		            Var.boxes[k].removeAllViews( );
		            
		            if (tile != 0 || Var.goal) Var.boxes[k].addView(Var.tiles[tile]);
		        }
		    }
	    });
    }

/////////////////////////////////////////////////////////////////////////////////////////
	
	private void stopIda( )
	{
		Var.solution.clear( );
        Var.thread = -1;
        gears_stop( );
        
        if(Var.ida != null)
        {
        	idaRunning.run( );
        }
	}
	
/////////////////////////////////////////////////////////////////////////////////////////
	
	private void slide_mechanism(final int move, final boolean auto)
	{
		final int gap = Math.abs(Var.positions[move] - Var.positions[0]);
		
		if (gap == 1)
		{
			if(Var.positions[move] / Var.side
			== Var.positions[   0] / Var.side)
		    {
		    	slide(move,Var.positions[move] , Var.positions[0] ,
			               Var.positions[move] > Var.positions[0] ? R.anim.slide_left
				                                                  : R.anim.slide_right,auto);
		    }
		}
		else
		if (gap == Var.side)
		{
			if(Var.positions[move] % Var.side
			== Var.positions[   0] % Var.side)
	        {
	            slide(move,Var.positions[move] , Var.positions[0] ,
			               Var.positions[move] > Var.positions[0] ? R.anim.slide_up
				                                                  : R.anim.slide_down,auto);
	        }
		}
	}
	
/////////////////////////////////////////////////////////////////////////////////////////

    private boolean isGoal( )
    {
        return(Var.size == 9 ? heuristic8 (Var.puzzle) == 0
		                     : heuristic15(Var.puzzle) == 0);
    }
	
/////////////////////////////////////////////////////////////////////////////////////////
	
	private void showInfo(final boolean newOne)
	{
		runOnUiThread(new Runnable( )
	    {
		    @Override
		    public void run( )
		    {
				Var.info = true;
		            
	            final TextView info = findViewById(R.id.informations);
		        
	            info.setVisibility(View.VISIBLE);
	            
	            if(newOne)
	            {
		            final Animation anim = AnimationUtils.loadAnimation(getApplicationContext( ),R.anim.slide_down_info);
		            
		            anim.setAnimationListener(new Animation.AnimationListener( )
		            {
		                @Override public void onAnimationStart (Animation arg){ }
		                @Override public void onAnimationRepeat(Animation arg){ }
		                @Override public void onAnimationEnd   (Animation arg)
		                {
		                    info.clearAnimation   ( );
		                    info.setMovementMethod(new ScrollingMovementMethod( ));
		                    info.setText          (R.string.about);
		                    info.scrollTo         (0,0);
		                }
		            });
		            
		            info.startAnimation(anim);
	            }
	            else
	            {
	                info.setMovementMethod(new ScrollingMovementMethod( ));
		            info.setText          (R.string.about);
	            }
		    }
	    });
	}
	
///////////////////////////////////////////////////////////////////////////////////////// theme

    private void show_what_needed( )
    {
        runOnUiThread(new Runnable( )
	    {
		    @Override
		    public void run( )
		    {
			    if(Var.size == 9){findViewById(R.id.board_eight  ).setVisibility(View.VISIBLE);
				                  findViewById(R.id.board_fifteen).setVisibility(View.GONE   );}
			    else             {findViewById(R.id.board_fifteen).setVisibility(View.VISIBLE);
				                  findViewById(R.id.board_eight  ).setVisibility(View.GONE   );}
		    }
	    });
    }

/////////////////////////////////////////////////////////////////////////////////////////

    private void ida_button( )
    {
        runOnUiThread(new Runnable( )
	    {
		    @Override
		    public void run( )
		    {
			    final ImageButton button = findViewById(R.id.idaStar);
			
			    button.setImageResource(Var.goal           ? R.drawable.button    :
					                    Var.thread !=  -1  ? R.drawable.stop      :
							            Var.ida    != null ? R.drawable.hourglass : R.drawable.magic_stick);
		    }
	    });
    }

/////////////////////////////////////////////////////////////////////////////////////////

    private void mode_button( )
    {
        runOnUiThread(new Runnable( )
	    {
		    @Override
		    public void run( )
		    {
			    final ImageButton button = findViewById(R.id.mode);
			
			    button.setImageResource(Var.side == 4 ? R.drawable.fifteen : R.drawable.eight);
		    }
	    });
    }

/////////////////////////////////////////////////////////////////////////////////////////

    private int getSticker(final int index)
    {
        switch(index)
        {
            case  0: return R.drawable.star;
            case  1: return R.drawable.t1  ;
            case  2: return R.drawable.t2  ;
            case  3: return R.drawable.t3  ;
            case  4: return R.drawable.t4  ;
            case  5: return R.drawable.t5  ;
            case  6: return R.drawable.t6  ;
            case  7: return R.drawable.t7  ;
            case  8: return R.drawable.t8  ;
            case  9: return R.drawable.t9  ;
            case 10: return R.drawable.t10 ;
            case 11: return R.drawable.t11 ;
            case 12: return R.drawable.t12 ;
            case 13: return R.drawable.t13 ;
            case 14: return R.drawable.t14 ;
            case 15: return R.drawable.t15 ;
        }

        return 0;
    }

/////////////////////////////////////////////////////////////////////////////////////////

    private ConstraintLayout getPos(final int index)
    {
        switch(index)
        {
            case  0: return Var.size == 16 ? (ConstraintLayout)findViewById(R.id.p00_fifteen) : (ConstraintLayout)findViewById(R.id.p00_eight);
            case  1: return Var.size == 16 ? (ConstraintLayout)findViewById(R.id.p01_fifteen) : (ConstraintLayout)findViewById(R.id.p01_eight);
            case  2: return Var.size == 16 ? (ConstraintLayout)findViewById(R.id.p02_fifteen) : (ConstraintLayout)findViewById(R.id.p02_eight);
            case  3: return Var.size == 16 ? (ConstraintLayout)findViewById(R.id.p03_fifteen) : (ConstraintLayout)findViewById(R.id.p10_eight);
            case  4: return Var.size == 16 ? (ConstraintLayout)findViewById(R.id.p10_fifteen) : (ConstraintLayout)findViewById(R.id.p11_eight);
            case  5: return Var.size == 16 ? (ConstraintLayout)findViewById(R.id.p11_fifteen) : (ConstraintLayout)findViewById(R.id.p12_eight);
            case  6: return Var.size == 16 ? (ConstraintLayout)findViewById(R.id.p12_fifteen) : (ConstraintLayout)findViewById(R.id.p20_eight);
            case  7: return Var.size == 16 ? (ConstraintLayout)findViewById(R.id.p13_fifteen) : (ConstraintLayout)findViewById(R.id.p21_eight);
            case  8: return Var.size == 16 ? (ConstraintLayout)findViewById(R.id.p20_fifteen) : (ConstraintLayout)findViewById(R.id.p22_eight);
            case  9: return findViewById(R.id.p21_fifteen);
            case 10: return findViewById(R.id.p22_fifteen);
            case 11: return findViewById(R.id.p23_fifteen);
            case 12: return findViewById(R.id.p30_fifteen);
            case 13: return findViewById(R.id.p31_fifteen);
            case 14: return findViewById(R.id.p32_fifteen);
            case 15: return findViewById(R.id.p33_fifteen);
        }

        return new ConstraintLayout(this);
    }

///////////////////////////////////////////////////////////////////////////////////////// animations

    private void slide(final int move, final int place, final int blank, final int towards, final boolean auto)
    {
	    runOnUiThread(new Runnable( )
	    {
		    @Override
		    public void run( )
		    {
		        final Animation movement = AnimationUtils.loadAnimation(getApplicationContext( ),towards);
						
		        movement.setAnimationListener(new Animation.AnimationListener( )
				{
					@Override public void onAnimationStart (Animation arg)
					{
						int[ ] puzzleClone = Var.puzzle   .clone( );
				        int[ ] positsClone = Var.positions.clone( );
				        
				        puzzleClone[blank] = move ;
				        puzzleClone[place] = 0    ;
				        positsClone[0    ] = place;
				        positsClone[move ] = blank;
				        
				        Var.positions = positsClone.clone( );
				        Var.puzzle    = puzzleClone.clone( );
				        Var.goal      = isGoal( );
						
						Var.boxes[place].removeView(Var.tiles[move]);
						Var.boxes[blank].addView   (Var.tiles[move]);
					}
					@Override public void onAnimationRepeat(Animation arg){ }
					@Override public void onAnimationEnd   (Animation arg)
					{
						if(auto && Var.thread != -1 && !Var.goal && Var.solution.size( ) > 0)
						{
							slide_mechanism(Var.solution.remove(Var.solution.size( )-1),true);
						}
					}
				});
		
		    	Var.boxes[blank].startAnimation(movement);
		    }
	    });
    }

/////////////////////////////////////////////////////////////////////////////////////////

	private void stop_slide( )
	{
		runOnUiThread(new Runnable( )
        {
            @Override
            public void run( )
            {
            	for(int k=0;k< Var.size;++k)
	            {
		            if(Var.boxes[k] != null) Var.boxes[k].clearAnimation( );
	            }
            }
        });
	}
	
/////////////////////////////////////////////////////////////////////////////////////////

    private void gears_spin(final boolean appear)
    {
        runOnUiThread(new Runnable( )
        {
            @Override
            public void run( )
            {
                final ImageView left_gear  = findViewById(R.id.left_gear );
                final ImageView right_gear = findViewById(R.id.right_gear);

                left_gear .setVisibility(View.VISIBLE);
                right_gear.setVisibility(View.VISIBLE);
				
                if(appear)
                {
                    final Animation anim = AnimationUtils.loadAnimation(getApplicationContext( ),R.anim.appear);
                    
                    anim.setAnimationListener(new Animation.AnimationListener( )
                    {
                        @Override public void onAnimationStart (Animation arg){ }
                        @Override public void onAnimationRepeat(Animation arg){ }
                        @Override public void onAnimationEnd   (Animation arg)
                        {
                            left_gear .startAnimation(AnimationUtils.loadAnimation(getApplicationContext( ),R.anim.anticlockwise));
                            right_gear.startAnimation(AnimationUtils.loadAnimation(getApplicationContext( ),R.anim.clockwise    ));
                        }
                    });

                    left_gear .startAnimation(AnimationUtils.loadAnimation(getApplicationContext( ),R.anim.appear));
                    right_gear.startAnimation(anim);
                }
                else
                {
                    left_gear .startAnimation(AnimationUtils.loadAnimation(getApplicationContext( ),R.anim.anticlockwise));
                    right_gear.startAnimation(AnimationUtils.loadAnimation(getApplicationContext( ),R.anim.clockwise    ));
                }
            }
        });
    }

/////////////////////////////////////////////////////////////////////////////////////////

    private void gears_stop( )
    {
        runOnUiThread(new Runnable( )
        {
            @Override
            public void run( )
            {
            	final ImageView right_gear = findViewById(R.id.right_gear);
    	
		        if(right_gear.getVisibility( ) == View.VISIBLE)
		        {
        	        final ImageView left_gear = findViewById(R.id.left_gear);

                    final Animation anim = AnimationUtils.loadAnimation(getApplicationContext( ),R.anim.disappear);

                    anim.setAnimationListener(new Animation.AnimationListener( )
                    {
                        @Override public void onAnimationStart (Animation arg){ }
                        @Override public void onAnimationRepeat(Animation arg){ }
                        @Override public void onAnimationEnd   (Animation arg)
                        {
                            left_gear .setVisibility(View.GONE);
                            right_gear.setVisibility(View.GONE);
                        }
                    });

                    left_gear .startAnimation(AnimationUtils.loadAnimation(getApplicationContext( ),R.anim.disappear));
                    right_gear.startAnimation(anim);
	            }
	        }
        });
    }

/////////////////////////////////////////////////////////////////////////////////////////

    private void goal_animation( )
    {
        runOnUiThread(new Runnable( )
        {
            @Override
            public void run( )
            {
                Var.boxes[Var.size-1].addView(Var.tiles[0]);
                
                Var.tiles[0].startAnimation(AnimationUtils.loadAnimation(getApplicationContext( ),R.anim.star));
                
                final ImageView happy_back  = findViewById(R.id.happy_gear_back );
                final ImageView happy_front = findViewById(R.id.happy_gear_front);
				final ImageView left_thumb  = findViewById(R.id.left_thumb      );
				final ImageView right_thumb = findViewById(R.id.right_thumb     );
				
				happy_back .setVisibility(View.VISIBLE);
				happy_front.setVisibility(View.VISIBLE);
				left_thumb .setVisibility(View.VISIBLE);
				right_thumb.setVisibility(View.VISIBLE);
				
				final Animation anim = AnimationUtils.loadAnimation(getApplicationContext( ),R.anim.shake_right);
				
				anim.setAnimationListener(new Animation.AnimationListener( )
				{
					@Override public void onAnimationStart (Animation arg){ }
					@Override public void onAnimationRepeat(Animation arg){ }
					@Override public void onAnimationEnd   (Animation arg)
					{
						happy_back .setVisibility(View.GONE);
						happy_front.setVisibility(View.GONE);
						left_thumb .setVisibility(View.GONE);
						right_thumb.setVisibility(View.GONE);
					}
				});
				
				happy_front.startAnimation(AnimationUtils.loadAnimation(getApplicationContext( ),R.anim.oscillator_front));
				happy_back .startAnimation(AnimationUtils.loadAnimation(getApplicationContext( ),R.anim.oscillator_back ));
				left_thumb .startAnimation(AnimationUtils.loadAnimation(getApplicationContext( ),R.anim.shake_left      ));
				right_thumb.startAnimation(anim);
            }
        });
    }

/////////////////////////////////////////////////////////////////////////////////////////
	
	private final Runnable beHappy = new Runnable( )
	{
	    @Override
	    public void run( )
	    {
	        if(Var.goal && Var.tiles[0].getParent( ) == null && Var.boxes[Var.size-1].getChildCount( ) == 0)
	        {
	        	Var.thread  = -1 ;
	        	goal_animation( );
	        	ida_button    ( );
	        }
	        else
	        {
	        	Var.checkGoal.postDelayed(beHappy,211);
	        }
	    }
    };

///////////////////////////////////////////////////////////////////////////////////////// idaStar : ida and executing

    private class IDAstar extends Thread
    {
        int[ ] path = new int[0];

        @Override
        public void run( )
        {
            Var.solution.clear( );
            
            if(Var.size == 9)
            {
                path = idaStar8(Var.puzzle);
            }
            
            else
            {
                do
                {
                    int bound = path.length == 2 && path[0] == 0 ? path[1] : heuristic15(Var.puzzle);
            	    
                    path  = idaStar15(Var.puzzle,bound);
                }
                while(this.getId( ) == Var.thread && path.length == 2 && path[0] == 0);
            }
            
            if(path.length != 0 && path[0] != 0)
            {
                for(int k=path.length-1;k>=0;--k)
                {
                    Var.solution.add(path[k]);
                }
				
                if(this.getId( ) == Var.thread)
                {
                	slide_mechanism(Var.solution.remove(path.length-1),true);
                }
            }
        }
    }

/////////////////////////////////////////////////////////////////////////////////////////
	
	private void find_solution(final boolean show_gears, final boolean appear)
	{
		if(show_gears) gears_spin(appear);
		
		Var.ida = new IDAstar( );
		Var.ida.start        ( );
		Var.thread = Var.ida.getId( );
		Var.checkIda.postDelayed  (idaRunning,114);
	}

/////////////////////////////////////////////////////////////////////////////////////////
	
	private final Runnable idaRunning = new Runnable( )
	{
	    @Override
	    public void run( )
	    {
	        if(Var.ida != null && Var.ida.isAlive( ))
	        {
	        	Var.checkIda.postDelayed(idaRunning,423);
	        }
	        else
	        {
	        	Var.ida = null;
	        	gears_stop(  );
	        	ida_button(  );
	        }
	    }
    };

/////////////////////////////////////////////////////////////////////////////////////////

}
