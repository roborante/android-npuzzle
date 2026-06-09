/*
 *  puzzle8.h
 *
 *  Created on: Apr 4, 2019
 *      Author: ro.bra.mo.
 */

/////////////////////////////////////////////////////////////////////////////////////////

#include <random>
#include <vector>
#include <array>

/////////////////////////////////////////////////////////////////////////////////////////

#ifndef BLANK
#define BLANK 0
#endif  /* BLANK */

/////////////////////////////////////////////////////////////////////////////////////////

#ifndef PUZZLE_8_H_
#define PUZZLE_8_H_

/////////////////////////////////////////////////////////////////////////////////////////

class Puzzle_8
{

/////////////////////////////////////////////////////////////////////////////////////////

public:

             Puzzle_8(                      );
    explicit Puzzle_8(const unsigned char[9]);

    unsigned char board[3][3];

    //bool created  ( ) const;
    //void showBoard( ) const;

	inline unsigned char heuristic(unsigned char(*)[3]);

	std::vector<char> idaStar( );

    virtual ~Puzzle_8( );

/////////////////////////////////////////////////////////////////////////////////////////

private:

    struct node{unsigned char state[3][3]{ };
                         char move          ;
                unsigned char x             ;
                unsigned char y             ;
                node(  ){move=0; x=0; y=0;}};

    const unsigned char goal[9][2] = {{2,2},{0,0},{0,1},{0,2},
											{1,0},{1,1},{1,2},
											{2,0},{2,1}     };

    bool solvable(const unsigned char[ ]) const;

    inline std::vector<node> expand  (const node*);
    inline std::vector<char> findPath(std::vector<node>&, const unsigned char&, const unsigned char&, unsigned char&);

    std::vector<char> buildPath(const std::vector<node>&) const;
};

/////////////////////////////////////////////////////////////////////////////////////////

#endif /* PUZZLE_8_H_ */
