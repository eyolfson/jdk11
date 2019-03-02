/*
 * Copyright © 2012  Google, Inc.
 *
 *  This is part of HarfBuzz, a text shaping library.
 *
 * Permission is hereby granted, without written agreement and without
 * license or royalty fees, to use, copy, modify, and distribute this
 * software and its documentation for any purpose, provided that the
 * above copyright notice and the following two paragraphs appear in
 * all copies of this software.
 *
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER BE LIABLE TO ANY PARTY FOR
 * DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
 * ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN
 * IF THE COPYRIGHT HOLDER HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH
 * DAMAGE.
 *
 * THE COPYRIGHT HOLDER SPECIFICALLY DISCLAIMS ANY WARRANTIES, INCLUDING,
 * BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE.  THE SOFTWARE PROVIDED HEREUNDER IS
 * ON AN "AS IS" BASIS, AND THE COPYRIGHT HOLDER HAS NO OBLIGATION TO
 * PROVIDE MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS, OR MODIFICATIONS.
 *
 * Google Author(s): Behdad Esfahbod
 */

#include "hb.hh"
#include "hb-shape-plan.hh"
#include "hb-shaper.hh"
#include "hb-font.hh"
#include "hb-buffer.hh"


/**
 * SECTION:hb-shape-plan
 * @title: hb-shape-plan
 * @short_description: Object representing a shaping plan
 * @include: hb.h
 *
 * Shape plans are not used for shaping directly, but can be access to query
 * certain information about how shaping will perform given a set of input
 * parameters (script, language, direction, features, etc.)
 * Most client would not need to deal with shape plans directly.
 **/


/*
 * hb_shape_plan_key_t
 */

bool
hb_shape_plan_key_t::init (bool                           copy,
                           hb_face_t                     *face,
                           const hb_segment_properties_t *props,
                           const hb_feature_t            *user_features,
                           unsigned int                   num_user_features,
                           const int                     *coords,
                           unsigned int                   num_coords,
                           const char * const            *shaper_list)
{
  hb_feature_t *features = nullptr;
  if (copy && num_user_features && !(features = (hb_feature_t *) calloc (num_user_features, sizeof (hb_feature_t))))
    goto bail;

  this->props = *props;
  this->num_user_features = num_user_features;
  this->user_features = copy ? features : user_features;
  if (copy && num_user_features)
  {
    memcpy (features, user_features, num_user_features * sizeof (hb_feature_t));
    /* Make start/end uniform to easier catch bugs. */
    for (unsigned int i = 0; i < num_user_features; i++)
    {
      if (features[0].start != HB_FEATURE_GLOBAL_START)
        features[0].start = 1;
      if (features[0].end   != HB_FEATURE_GLOBAL_END)
        features[0].end   = 2;
    }
  }
  this->shaper_func = nullptr;
  this->shaper_name = nullptr;
  this->ot.init (face, coords, num_coords);

  /*
   * Choose shaper.
   */

#define HB_SHAPER_PLAN(shaper) \
        HB_STMT_START { \
          if (face->data.shaper) \
          { \
            this->shaper_func = _hb_##shaper##_shape; \
            this->shaper_name = #shaper; \
            return true; \
          } \
        } HB_STMT_END

  if (unlikely (shaper_list))
  {
    for (; *shaper_list; shaper_list++)
      if (false)
        ;
#define HB_SHAPER_IMPLEMENT(shaper) \
      else if (0 == strcmp (*shaper_list, #shaper)) \
        HB_SHAPER_PLAN (shaper);
#include "hb-shaper-list.hh"
#undef HB_SHAPER_IMPLEMENT
  }
  else
  {
    const hb_shaper_entry_t *shapers = _hb_shapers_get ();
    for (unsigned int i = 0; i < HB_SHAPERS_COUNT; i++)
      if (false)
        ;
#define HB_SHAPER_IMPLEMENT(shaper) \
      else if (shapers[i].func == _hb_##shaper##_shape) \
        HB_SHAPER_PLAN (shaper);
#include "hb-shaper-list.hh"
#undef HB_SHAPER_IMPLEMENT
  }
#undef HB_SHAPER_PLAN

bail:
  ::free (features);
  return false;
}

bool
hb_shape_plan_key_t::user_features_match (const hb_shape_plan_key_t *other)
{
  if (this->num_user_features != other->num_user_features)
    return false;
  for (unsigned int i = 0; i < num_user_features; i++)
  {
    if (this->user_features[i].tag   != other->user_features[i].tag   ||
        this->user_features[i].value != other->user_features[i].value ||
        (this->user_features[i].start == HB_FEATURE_GLOBAL_START &&
         this->user_features[i].end   == HB_FEATURE_GLOBAL_END) !=
        (other->user_features[i].start == HB_FEATURE_GLOBAL_START &&
         other->user_features[i].end   == HB_FEATURE_GLOBAL_END))
      return false;
  }
  return true;
}

bool
hb_shape_plan_key_t::equal (const hb_shape_plan_key_t *other)
{
  return hb_segment_properties_equal (&this->props, &other->props) &&
         this->user_features_match (other) &&
         this->ot.equal (&other->ot) &&
         this->shaper_func == other->shaper_func;
}


/*
 * hb_shape_plan_t
 */


/**
 * hb_shape_plan_create: (Xconstructor)
 * @face:
 * @props:
 * @user_features: (array length=num_user_features):
 * @num_user_features:
 * @shaper_list: (array zero-terminated=1):
 *
 *
 *
 * Return value: (transfer full):
 *
 * Since: 0.9.7
 **/
hb_shape_plan_t *
hb_shape_plan_create (hb_face_t                     *face,
                      const hb_segment_properties_t *props,
                      const hb_feature_t            *user_features,
                      unsigned int                   num_user_features,
                      const char * const            *shaper_list)
{
  return hb_shape_plan_create2 (face, props,
                                user_features, num_user_features,
                                nullptr, 0,
                                shaper_list);
}

hb_shape_plan_t *
hb_shape_plan_create2 (hb_face_t                     *face,
                       const hb_segment_properties_t *props,
                       const hb_feature_t            *user_features,
                       unsigned int                   num_user_features,
                       const int                     *coords,
                       unsigned int                   num_coords,
                       const char * const            *shaper_list)
{
  DEBUG_MSG_FUNC (SHAPE_PLAN, nullptr,
                  "face=%p num_features=%d num_coords=%d shaper_list=%p",
                  face,
                  num_user_features,
                  num_coords,
                  shaper_list);

  assert (props->direction != HB_DIRECTION_INVALID);

  hb_shape_plan_t *shape_plan;

  if (unlikely (!props))
    goto bail;
  if (!(shape_plan = hb_object_create<hb_shape_plan_t> ()))
    goto bail;

  if (unlikely (!face))
    face = hb_face_get_empty ();
  hb_face_make_immutable (face);
  shape_plan->face_unsafe = face;

  if (unlikely (!shape_plan->key.init (true,
                                       face,
                                       props,
                                       user_features,
                                       num_user_features,
                                       coords,
                                       num_coords,
                                       shaper_list)))
    goto bail2;
  if (unlikely (!shape_plan->ot.init0 (face, &shape_plan->key)))
    goto bail3;

  return shape_plan;

bail3:
  shape_plan->key.free ();
bail2:
  free (shape_plan);
bail:
  return hb_shape_plan_get_empty ();
}

/**
 * hb_shape_plan_get_empty:
 *
 *
 *
 * Return value: (transfer full):
 *
 * Since: 0.9.7
 **/
hb_shape_plan_t *
hb_shape_plan_get_empty ()
{
  return const_cast<hb_shape_plan_t *> (&Null(hb_shape_plan_t));
}

/**
 * hb_shape_plan_reference: (skip)
 * @shape_plan: a shape plan.
 *
 *
 *
 * Return value: (transfer full):
 *
 * Since: 0.9.7
 **/
hb_shape_plan_t *
hb_shape_plan_reference (hb_shape_plan_t *shape_plan)
{
  return hb_object_reference (shape_plan);
}

/**
 * hb_shape_plan_destroy: (skip)
 * @shape_plan: a shape plan.
 *
 *
 *
 * Since: 0.9.7
 **/
void
hb_shape_plan_destroy (hb_shape_plan_t *shape_plan)
{
  if (!hb_object_destroy (shape_plan)) return;

  shape_plan->ot.fini ();
  shape_plan->key.free ();
  free (shape_plan);
}

/**
 * hb_shape_plan_set_user_data: (skip)
 * @shape_plan: a shape plan.
 * @key:
 * @data:
 * @destroy:
 * @replace:
 *
 *
 *
 * Return value:
 *
 * Since: 0.9.7
 **/
hb_bool_t
hb_shape_plan_set_user_data (hb_shape_plan_t    *shape_plan,
                             hb_user_data_key_t *key,
                             void *              data,
                             hb_destroy_func_t   destroy,
                             hb_bool_t           replace)
{
  return hb_object_set_user_data (shape_plan, key, data, destroy, replace);
}

/**
 * hb_shape_plan_get_user_data: (skip)
 * @shape_plan: a shape plan.
 * @key:
 *
 *
 *
 * Return value: (transfer none):
 *
 * Since: 0.9.7
 **/
void *
hb_shape_plan_get_user_data (hb_shape_plan_t    *shape_plan,
                             hb_user_data_key_t *key)
{
  return hb_object_get_user_data (shape_plan, key);
}

/**
 * hb_shape_plan_get_shaper:
 * @shape_plan: a shape plan.
 *
 *
 *
 * Return value: (transfer none):
 *
 * Since: 0.9.7
 **/
const char *
hb_shape_plan_get_shaper (hb_shape_plan_t *shape_plan)
{
  return shape_plan->key.shaper_name;
}


/**
 * hb_shape_plan_execute:
 * @shape_plan: a shape plan.
 * @font: a font.
 * @buffer: a buffer.
 * @features: (array length=num_features):
 * @num_features:
 *
 *
 *
 * Return value:
 *
 * Since: 0.9.7
 **/
hb_bool_t
hb_shape_plan_execute (hb_shape_plan_t    *shape_plan,
                       hb_font_t          *font,
                       hb_buffer_t        *buffer,
                       const hb_feature_t *features,
                       unsigned int        num_features)
{
  DEBUG_MSG_FUNC (SHAPE_PLAN, shape_plan,
                  "num_features=%d shaper_func=%p, shaper_name=%s",
                  num_features,
                  shape_plan->key.shaper_func,
                  shape_plan->key.shaper_name);

  if (unlikely (!buffer->len))
    return true;

  assert (!hb_object_is_immutable (buffer));
  assert (buffer->content_type == HB_BUFFER_CONTENT_TYPE_UNICODE);

  if (unlikely (hb_object_is_inert (shape_plan)))
    return false;

  assert (shape_plan->face_unsafe == font->face);
  assert (hb_segment_properties_equal (&shape_plan->key.props, &buffer->props));

#define HB_SHAPER_EXECUTE(shaper) \
        HB_STMT_START { \
          return font->data.shaper && \
                 _hb_##shaper##_shape (shape_plan, font, buffer, features, num_features); \
        } HB_STMT_END

  if (false)
    ;
#define HB_SHAPER_IMPLEMENT(shaper) \
  else if (shape_plan->key.shaper_func == _hb_##shaper##_shape) \
    HB_SHAPER_EXECUTE (shaper);
#include "hb-shaper-list.hh"
#undef HB_SHAPER_IMPLEMENT

#undef HB_SHAPER_EXECUTE

  return false;
}


/*
 * Caching
 */

/**
 * hb_shape_plan_create_cached:
 * @face:
 * @props:
 * @user_features: (array length=num_user_features):
 * @num_user_features:
 * @shaper_list: (array zero-terminated=1):
 *
 *
 *
 * Return value: (transfer full):
 *
 * Since: 0.9.7
 **/
hb_shape_plan_t *
hb_shape_plan_create_cached (hb_face_t                     *face,
                             const hb_segment_properties_t *props,
                             const hb_feature_t            *user_features,
                             unsigned int                   num_user_features,
                             const char * const            *shaper_list)
{
  return hb_shape_plan_create_cached2 (face, props,
                                       user_features, num_user_features,
                                       nullptr, 0,
                                       shaper_list);
}

hb_shape_plan_t *
hb_shape_plan_create_cached2 (hb_face_t                     *face,
                              const hb_segment_properties_t *props,
                              const hb_feature_t            *user_features,
                              unsigned int                   num_user_features,
                              const int                     *coords,
                              unsigned int                   num_coords,
                              const char * const            *shaper_list)
{
  DEBUG_MSG_FUNC (SHAPE_PLAN, nullptr,
                  "face=%p num_features=%d shaper_list=%p",
                  face,
                  num_user_features,
                  shaper_list);

retry:
  hb_face_t::plan_node_t *cached_plan_nodes = face->shape_plans;

  bool dont_cache = hb_object_is_inert (face);

  if (likely (!dont_cache))
  {
    hb_shape_plan_key_t key;
    if (!key.init (false,
                   face,
                   props,
                   user_features,
                   num_user_features,
                   coords,
                   num_coords,
                   shaper_list))
      return hb_shape_plan_get_empty ();

    for (hb_face_t::plan_node_t *node = cached_plan_nodes; node; node = node->next)
      if (node->shape_plan->key.equal (&key))
      {
        DEBUG_MSG_FUNC (SHAPE_PLAN, node->shape_plan, "fulfilled from cache");
        return hb_shape_plan_reference (node->shape_plan);
      }
  }

  hb_shape_plan_t *shape_plan = hb_shape_plan_create2 (face, props,
                                                       user_features, num_user_features,
                                                       coords, num_coords,
                                                       shaper_list);

  if (unlikely (dont_cache))
    return shape_plan;

  hb_face_t::plan_node_t *node = (hb_face_t::plan_node_t *) calloc (1, sizeof (hb_face_t::plan_node_t));
  if (unlikely (!node))
    return shape_plan;

  node->shape_plan = shape_plan;
  node->next = cached_plan_nodes;

  if (unlikely (!face->shape_plans.cmpexch (cached_plan_nodes, node)))
  {
    hb_shape_plan_destroy (shape_plan);
    free (node);
    goto retry;
  }
  DEBUG_MSG_FUNC (SHAPE_PLAN, shape_plan, "inserted into cache");

  return hb_shape_plan_reference (shape_plan);
}
