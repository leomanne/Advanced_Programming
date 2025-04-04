
module MultiSet
  ( MSet(..)
  , empty
  , add
  , occs
  , elems
  , subeq
  , union
  , mapMSet
  ) where

import Data.Foldable (Foldable(..))


-- MSet type

-- Representing a multiset with a constructor holding a list of
-- (value, count) pairs. No duplicates should appear in that list,
-- and each count must be > 0.
data MSet a = MS [(a, Int)]
  deriving (Show)



-- | empty
--   Makes an MSet with no elements at all.

empty :: MSet a
empty = MS []
-- That is just an empty list of pairs inside.


-- Builds an MSet from a regular list by adding elements one by one.

fromList :: Eq a => [a] -> MSet a
fromList = foldl add empty


-- Extracts the underlying list of (value, count) from the MS constructor.

toAssocList :: MSet a -> [(a, Int)]
toAssocList (MS xs) = xs


-- Required functions



-- add
-- Given a multiset and a value, we increment that value's count by 1.
-- If the value wasnthere before, it gets inserted with count=1.

add :: Eq a => MSet a -> a -> MSet a
add (MS []) v = MS [(v, 1)]
  -- If there's nothing in the multiset, just put (v,1).
add (MS ((x,n):xs)) v
  | x == v    = MS ((x, n+1) : xs)   -- found it, so raise count
  | otherwise = 
      let MS rest = add (MS xs) v
      in MS ((x,n):rest)
  -- if x isn't what we want, keep going and then rebuild.


-- occs
-- Returns how many times v appears in the multiset.
-- If v is missing, this is 0.

occs :: Eq a => MSet a -> a -> Int
occs (MS []) _ = 0
occs (MS ((x,n):xs)) v
  | x == v    = n
  | otherwise = occs (MS xs) v
  -- simple recursion until we find x==v


-- elems
-- Turns each pair (v,n) into n copies of v.
-- So we get a flat list with each value repeated as many times as needed.

elems :: MSet a -> [a]
elems (MS pairs) =
  concatMap (\(v,n) -> replicate n v) pairs


-- subeq
-- subeq m1 m2 is true if every element of m1 is also in m2 with at least
-- the same multiplicity.

subeq :: Eq a => MSet a -> MSet a -> Bool
subeq (MS []) _ = True
subeq (MS ((v,n):xs)) m2 =
  let found = occs m2 v
  in found >= n && subeq (MS xs) m2


-- union
-- Merges two multisets by adding counts for any shared elements.
-- The result still doesn't contain duplicates. 

union :: Eq a => MSet a -> MSet a -> MSet a
union (MS xs) m2 = foldl unionElem m2 xs
  where
    unionElem acc (v,n) = foldl add acc (replicate n v)
    -- we basically add 'v' n times to acc.


-- Making MSet an instance of Eq



-- Two MSet values are equal if they have exactly the same elements
-- with the same multiplicities, no matter the order.
-- We do this by checking both ways with subeq.

instance Eq a => Eq (MSet a) where
    (==) :: Eq a => MSet a -> MSet a -> Bool
    m1 == m2 = subeq m1 m2 && subeq m2 m1


-- Making MSet an instance of Foldable



-- We ignore multiplicities in our fold. That means we just treat
-- each distinct element exactly once. We'll do foldr here.

instance Foldable MSet where
  foldr :: (a -> b -> b) -> b -> MSet a -> b
  foldr f z (MS pairs) = foldr (\(v,_) acc -> f v acc) z pairs






-- mapMSet



-- mapMSet
-- Applies a function f to each occurrence in the multiset so if something
-- appears n times we apply f n times. If multiple elements map to the
-- same new value, their counts get merged. 

mapMSet :: (Eq b) => (a -> b) -> MSet a -> MSet b
mapMSet f m = fromList (map f (elems m))
  -- expand the multiset to a list with elems, map f over it,
  -- then use fromList to merge duplicates.

{-
We don't define instance Functor MSet because
if two different elements end up mapped to the same value,
we have to merge them, which isn't purely elementwise.
That can break the structure.
-}
