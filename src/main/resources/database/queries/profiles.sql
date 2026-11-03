-- name: top-coins
SELECT name, coins FROM {prefix}profiles ORDER BY coins DESC LIMIT :limit

-- name: rank-of
SELECT COUNT(*) + 1 FROM {prefix}profiles
WHERE coins > (SELECT coins FROM {prefix}profiles WHERE uuid = :uuid)
